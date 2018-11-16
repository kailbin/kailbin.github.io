---
title: slf4j
categories:
  - Java
date: 2018-11-16
id: java/slf4j
---

slf4j 全称 Simple Logging Facede for Java，是 Java 日志的简易门面类库。可以使用统一的输出接口，根据底层依赖的不同，使用不同的日志框架。


<!-- more -->


# slf4j 是如何适配日志框架

当项目只有 `slf4j-api` 的依赖时，在 `Logger logger = LoggerFactory.getLogger(xxx.class)` 获取 `Logger` 的时候会报以下错误。
``` text
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
```

该错误提示无法加载 `org.slf4j.impl.StaticLoggerBinder` 类，并且把文档的地址也提示出来了: [http://www.slf4j.org/codes.html#StaticLoggerBinder](http://www.slf4j.org/codes.html#StaticLoggerBinder)。解决方法是 添加 `slf4j-nop.jar` `slf4j-simple.jar`、`slf4j-log4j12.jar`、`logback-classic.jar` 等任何日志框架中的 **任何一个** 到类路径中。

> 注意：SLF4J 1.6, 默认使用 no-operation (NOP) 的日志实现，并且不会有任何提示。

`slf4j-api` 目的是提供了统一的日志访问接口， `org.slf4j.impl.StaticLoggerBinder` 类才是适配底层日志框架的关键。

可以添加 `slf4j-log4j12`（间接依赖 `log4j`） 依赖，查看 jar 包的文件，会在 `slf4j-log4j12.jar` 中找到，`org.slf4j.impl.StaticLoggerBinder` 类。同理 `slf4j-simple.jar`、`logback-classic.jar`等 底层日志实现的包内也都能找到 **全路径名完全一致**的 `org.slf4j.impl.StaticLoggerBinder`类。虽然 **全路径名完全一致**，但是具体实现各有不同，这就可以达到：**通过修改底层依赖来切换不同的日志框架**。


# 当项目依赖了多个日志框架时

当 封装jar包的提供服务时，理论上只应该暴露 `slf4j-api` 的间接依赖，具体实现应该有调用方来指定。

但是由于各种原因，项目的间接依赖会出现各种日志的实现方式。比如 `slf4j-log4j12` 和 `logback-classic` 同时存在于类路径下时，这时会报以下提示，原因是 `slf4j-log4j12` 和 `logback-classic` 都存在 `org.slf4j.impl.StaticLoggerBinder` 类：

```
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:~/.m2/repository/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:~/.m2/repository/org/slf4j/slf4j-log4j12/1.7.25/slf4j-log4j12-1.7.25.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [ch.qos.logback.classic.util.ContextSelectorStaticBinder]
```
详见 多种底层绑定 的说明文档：http://www.slf4j.org/codes.html#multiple_bindings

存在多个绑定时，slf4j 也会选择其中一个日志记录框架并与之绑定，具体选择绑定的方式是由 JVM 的类加载顺序决定的，可认为完全是随机的。

常见解决方案是 **排除到不需要的底层绑定**，只留下一个，比如把 `slf4j-log4j12` 排除掉。

> 不同jar包名 相同类路径下，实际上 JVM 的加载也是有顺序的：**Classloader在加载资源时，查找资源的位置顺序与 `-classpath` 中指定的顺序一致**。这就表现为不同的环境 slf4j 绑定的具体实现是不同的。比如：
> 
> - 在 IDEA 中，就表现为，pom 文件哪个依赖写在前面，slf4j 就使用哪个，原因是 IDEA 执行的时候会把 pom 文件依赖的数据作为 -classpath 参数的顺序
> - 打成 jar 包 或者 Web 容器的下运行，可能又是其他的效果，因为不同的运行方式，加载顺序不同。
> 所以官方说：可以认为是随机的。 所以建议手动把不需要的排除掉。
> 
> [Java利用classloader从classpath加载资源](https://blog.csdn.net/hongxingxiaonan/article/details/50486997)


# 旧项目没有使用 slf4j API 怎么办

有些遗留项目不是通过 slf4j API 使用日志框架的，比如这种： `org.apache.log4j.Logger.getLogger(XXX.class);` 直接调用的 Log4j 的 API。

这种情况下，如果想把日志从 Log4j 切换到 logback，除了修改代码 把所有直接使用 Log4j API 的地方改成使用 slf4j API 外，还可以使用 slf4j 提供的转换工具 `log4j-over-slf4j`，该工具的主要作用是通过 log4j 形式的接口把日志 转发到 slf4j，再通过 slf4j 适配底层日志框架。

查看 `log4j-over-slf4j` jar 包内的代码会发现，jar 包内 类的全路径名与 log4j 几乎是一致的，即 log4j 的接口没有变，但是具体的实现全部变了。

需要注意的是： 需要把原来类路径下 log4j 的 jar 先包排除掉


# 检查 Logger 的名称是否与实际调用的类匹配

该功能是默认是关闭的，可通过设置系统属性 `System.setProperty("slf4j.detectLoggerNameMismatch", "true");` 或者 启动参数 `-Dslf4j.detectLoggerNameMismatch=true` 开启，示例如下：

``` java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    public static void main(String[] args) {
        // 开始 Logger 名不匹配提示
        System.setProperty("slf4j.detectLoggerNameMismatch", "true");
        
        // 因为 调用方是 Main.class , 但是获取的是 Logger.class 的 Logger，
        // 这种使用方式会对排查文件造成误导，会进行提示
        Logger logger = LoggerFactory.getLogger(Logger.class);
    }
}
```
对于不规范的代码，会进行以下提示：
``` text
SLF4J: Detected logger name mismatch. Given name: "org.slf4j.Logger"; computed name: "log.Main".
SLF4J: See http://www.slf4j.org/codes.html#loggerNameMismatch for an explanation
```
可查看官方文档，详细了解：http://www.slf4j.org/codes.html#loggerNameMismatch

# 相关 Jar 包介绍

## slf4j 接口
- slf4j-api： 写日志的时候，都使用该 jar 包内的接口

## 适配器绑定

适配器的主要功能是与具体的日志框架绑定，因其 jar 包内都有 `org.slf4j.impl.StaticLoggerBinder` 类，为避免冲突，只能有一个。

- slf4j-nop ： 空的实现，什么也不输出
- slf4j-simple ：适配  System.out | System.err 实现
- slf4j-log4j12 ： 适配 log4j 实现
- slf4j-jdk14 ： 适配 java.util.logging 实现
- logback-classic ： 适配 logback 实现
- slf4j-jcl ： 适配 commons-logging 实现
- ...

## 桥接器

- jcl-over-slf4j ： commons-logging 转发到 slf4j
- log4j-over-slf4j： log4j 转发到 slf4j
- jul-to-slf4j： java.util.logging 桥接 slf4j
- ...

# Read More
- [SLF4J user manual](https://www.slf4j.org/manual.html)
- [Bridging legacy APIs](https://www.slf4j.org/legacy.html)