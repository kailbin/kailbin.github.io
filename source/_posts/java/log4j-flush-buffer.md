---
title: Log4j BufferedIO 与 flush BufferedIO
date: 2017-06-10
desc: Log4j 刷新缓存, Log4j BufferedIO 与 flush BufferedIO

tags: [Java]
categories: Java
---

使用 Log4j 默认写日志的时候，默认会立即刷到文件中,如果日志写入量很大的话，会带来一定的性能损耗。所以Log4j提供了`BufferedIO`配置选项，如果配置`BufferedIO`为`true`，则使用 `BufferedWriter` 进行包装，写入数据量到达 `bufferSize` 之后，才会真正写到文件中：
``` java
// public class FileAppender extends WriterAppender
if(bufferedIO) {
    fw = new BufferedWriter(fw, bufferSize);
}
```
> Avoiding the flush operation at the end of each append results in a performance gain of 10 to 20 percent. However, there is safety tradeoff involved in skipping flushing. Indeed, when flushing is skipped, then it is likely that the last few log events will not be recorded on disk when the application exits. This is a high price to pay even for a 20% performance gain.

官方解释了使用 Buffer 会带来 10% ~ 20% 的性能提升，但是代价也是很明显是，假如系统退出，由于最后写入的日志 Buffer 还没满，无法写入文件会导致关键日志丢失。

文本主要解决使用Buffer之后，所带来的以下两个问题：
1. 当程序关闭的时候，由于一部分日志还在内存中导致的日志丢失问题
2. 由于日志无法实时输出，给问题排查带来了困难，程序报错无法立马看到报错的信息

<!--more-->

# Log4j 配置示例

这里使用的 Log4j 版本是 `1.7.21`。

``` xml
    <appender name="railyFile" class="org.apache.log4j.DailyRollingFileAppender">
        <param name="File" value="/opt/logs/flush_test/error.log"/>
        <!-- 开启Buffer -->
        <param name="BufferedIO" value="true"/>
        <!-- Buffer 大小为 8k，默认就是 8k -->
        <param name="BufferSize" value="8192"/>
        <param name="DatePattern" value="'daily.'yyyy-MM-dd'.log'"/>
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="[%d{yyyy-MM-dd HH:mm:ss\}][%-5p] [%t] [%c:%L]-%m%n"/>
        </layout>
    </appender>

    <root>
        <level value="error"/>
        <appender-ref ref="railyFile"/>
    </root>
```

# JVM 退出时 flush Buffer

JVM 支持关闭回调，需要在JVM退出的时候执行一些操作，可以在系统启动之后，注册`ShutdownHook`，如下：

``` java
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    LogManager.shutdown(); // QuietWriter.close() --> [ Writer.close ]
}));
```

# 运行时 flush Buffer

## WriterAppender 继承体系

<center>
    ![WriterAppender 继承体系](/images/java/log4j-flush-buffer/1.jpg)
</center>
可以看出常用的几个 `Appender` 都继承自 `WriterAppender`，但是需要注意的是，`BufferedIO` 和 `BufferSize` 并不是WriterAppender的配置，而是`FileAppender`的配置，所以只有`FileAppender`及其子类才支持Buffer输出。
WriterAppender 可以配置 ImmediateFlush，但是貌似 WriterAppender 和 ConsoleAppender 并没有理会这个配置，不管配不配，都不会有任何效果。
如果配了 BufferedIO=true 之后，不管 ImmediateFlush 设置的是什么都会覆盖为false。

``` java
// public class FileAppender extends WriterAppender
public void setBufferedIO(boolean bufferedIO) {
    this.bufferedIO = bufferedIO;
    if(bufferedIO) {
        immediateFlush = false;
    }
}
```
## flush Buffer

FileAppender 暴露了设置 Buffer 的 api，但是并没有暴露 flush buffer。
通过查看源码 最终找到了 WriterAppender 中的 `protected QuietWriter qw` 属性，通过该类可以进行flush。
以下代码从Log4j中获取到所有的`immediateFlush`为false的Appender，通过反射获取到QuietWriter，然后进行flush。

``` java
import org.apache.log4j.LogManager;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.helpers.QuietWriter;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Kail on 2017/6/10.
 */
public class Log4jBufferFlush {

    // 缓存 Appenders 的 QuietWriter
    private static final Set<QuietWriter> cacheWriteAppenders = new HashSet<>();

    public static void getWriterFromAppender() throws NoSuchFieldException, IllegalAccessException {
        if (cacheWriteAppenders.isEmpty()) {
            // 获取所有的 根 appenders
            Set<QuietWriter> quietWriters = getWriterFromAppender(LogManager.getRootLogger().getAllAppenders());
            cacheWriteAppenders.addAll(quietWriters);

            // 获取所有的 logger
            Enumeration currentLoggers = LogManager.getLoggerRepository().getCurrentLoggers();
            for (; currentLoggers.hasMoreElements(); ) {
                // 获取 logger 的 输出源
                quietWriters = getWriterFromAppender(((org.apache.log4j.Logger) currentLoggers.nextElement()).getAllAppenders());
                cacheWriteAppenders.addAll(quietWriters);
            }
        }

        // 第一次是通过反射获取的，如果已经获取过，直接从缓存中拿，避免反射带来的性能损耗
        for (QuietWriter quietWriter : cacheWriteAppenders) {
            quietWriter.flush();
        }
    }

    private static Set<QuietWriter> getWriterFromAppender(Enumeration rootAllAppends) throws NoSuchFieldException, IllegalAccessException {
        Set<QuietWriter> cacheWriteAppenders = new HashSet<>();
        for (; rootAllAppends.hasMoreElements(); ) {
            Object appender = rootAllAppends.nextElement();
            if (appender instanceof WriterAppender) {
                WriterAppender writerAppender = (WriterAppender) appender;
                // 是否立即 flush
                boolean immediateFlush = writerAppender.getImmediateFlush();
                if (!immediateFlush) {
                    Class writerAppenderClass = writerAppender.getClass();
                    // 从当前的 Appender 一直向上查，直到 WriterAppender
                    for (; !writerAppenderClass.equals(WriterAppender.class); ) {
                        writerAppenderClass = writerAppenderClass.getSuperclass();
                    }
                    Field qw = writerAppenderClass.getDeclaredField("qw");
                    qw.setAccessible(true); // WriterAppender中QuietWriter的访问修饰符是protected，需要现设置可访问

                    cacheWriteAppenders.add((QuietWriter) qw.get(appender)); // 通过反射从 appender 中拿到 QuietWriter ，然后进行缓存
                }
            }
        }
        return cacheWriteAppenders;
    }
}
```
这个示例会flush所有的Appender，使用的时候可以暴露一个Http接口，通过Http 接口调用该工具，来排查问题。

# 其它应用场景和拓展

以上提供了一种 flush buffer 的思路，如果 Redis 持久化思路，可以继承 FileAppender 或其子类，配置定时持久化。
如果 Buffer 满则 flush，或者在 一定时间内就算Buffer不满也持久化，避免日志量少的时候一直不输出日志。


# 拓展阅读

> [log4j日志优化：使用BufferedIO和BufferSize而不是ImmediateFlush](http://blog.csdn.net/aitangyong/article/details/50394857)
>
> [org.apache.log4j.WriterAppender](http://logging.apache.org/log4j/1.2/apidocs/index.html)


