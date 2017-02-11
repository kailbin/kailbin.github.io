---
title: Java 内建"故障排除"工具 — jcmd、jinfo、jmap、jhat、jstack、jsadebugd
date: 2017-02-10
desc: jcmd,jinfo,jhat,jmap,jsadebugd,jstack
---

jcmd: 向 JVM 发送诊断命令
jinfo: 获得 JVM 配置信息
jmap: 主要用于打印Java进程的 堆 信息
jhat: 方便析 Java 堆 的工具
jstack: 主要用于打印Java进程的 栈 信息
jsadebugd: 依附到一个Java进程或核心文件，担当一个调试服务器的作用

以上几个只有 `jcmd` 没有被官方说明为 `Experimental`(实验性的)。

<!--more-->

# jcmd

jcmd 是JDK 7 之后推出的一个多功能工具，拥有 `jmap` 的大部分功能。



#### 列出 Java 进程

直接执行 `jcmd` 即可列出java进行，效果与 `jcmd -l` 、 `jps -ml` 的效果是一样的


#### 打印出 java 进行支持的命令

`jcmd 0 help` ： 打印出所有java进程支持的命令
`jcmd <pid> help` ： 打印出执行java进程支持的命令

以下是在我的机器上执行的结果，结果打印出了 43976 Java 进程可用的命令。
``` bash
$ jcmd 43976 help
43976:
The following commands are available:
  
VM.native_memory
VM.commercial_features
ManagementAgent.stop
ManagementAgent.start_local
ManagementAgent.start
Thread.print
GC.class_histogram
GC.heap_dump
GC.run_finalization
GC.run
VM.uptime
VM.flags
VM.system_properties
VM.command_line
VM.version
help

  
For more information about a specific command use 'help <command>'.
```

**不同环境、不同进程 所可用的命令会不太一样**

#### 获取可用命令的帮助

`jcmd <pid> help command `

例如:
``` bash
$ jcmd 43976 help GC.heap_dump 
43976:
GC.heap_dump
Generate a HPROF format dump of the Java heap.
  
Impact: High: Depends on Java heap size and content. Request a full GC unless the '-all' option is specified.
  
Syntax : GC.heap_dump [options] <filename>
  
Arguments:
	filename :  Name of the dump file (STRING, no default value)
  
Options: (options must be specified using the <key> or <key>=<value> syntax)
	-all : [optional] Dump all objects, including unreachable objects (BOOLEAN, false)
```

#### 使用 jcmd 支持的命令

`jcmd <pid> command `

例如:


**打印 JVM 版本**
``` bash
$ jcmd 43976 VM.version
43976:
Java HotSpot(TM) 64-Bit Server VM version 24.75-b04
JDK 7.0_75
```

**打印 JVM 参数**
``` bash
$ jcmd 43976 VM.command_line
43976:
VM Arguments:
jvm_args: -Djava.awt.headless=true -Didea.version==2016.3.3 -Xmx768m -Didea.maven.embedder.version=3.3.9 -Dfile.encoding=UTF-8 
java_command: org.jetbrains.idea.maven.server.RemoteMavenServer
Launcher Type: SUN_STANDARD
```


#### 部分命令功能

| 命令 |  | 英文解释 |
|----:|:----|:----|
|GC.heap_dump           |  | Generate a HPROF format dump of the Java heap.|
|Thread.print           |  | Print all threads with stacktraces.|
|||
|GC.run                 |  | all java.lang.System.gc().|
|GC.class_histogram     |  | Provide statistics about the Java heap usage.|
|GC.run_finalization    |  | Call java.lang.System.runFinalization().|
|||
|VM.uptime              |  | Print VM uptime.|
|VM.system_properties   |  | Print system properties.|
|VM.command_line        |  | Print the command line used to start this VM instance.|
|VM.flags               |  | Print VM flag options and their current values.|
|VM.version             |  | Print JVM version information.|

#### 参考

> 官方文档 [jcmd](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/jcmd.html)

> [Java SE 7: Reviewing JVM Performance Command Line Tools](http://www.oracle.com/webfolder/technetwork/tutorials/obe/java/JavaJCMD/index.html)




# jinfo

`jinfo` 可以打印或者修改 Java进程 的配置信息。配置信息包括 Java 系统属性 和 JVM flags（ Java system properties and Java Virtual Machine (JVM) command-line flags）。

#### 查看帮助

`jinfo`、`jinfo -h`、`jinfo -help` 三种方法都可以打印出帮助信息。

``` bash
$ jinfo
Usage:
    jinfo [option] <pid>
        (to connect to running process)
    jinfo [option] <executable <core>
        (to connect to a core file)
    jinfo [option] [server_id@]<remote server IP or hostname>
        (to connect to remote debug server)

where <option> is one of:
    -flag <name>         to print the value of the named VM flag
    -flag [+|-]<name>    to enable or disable the named VM flag
    -flag <name>=<value> to set the named VM flag to the given value
    -flags               to print VM flags
    -sysprops            to print Java system properties
    <no option>          to print both of the above
    -h | -help           to print this help message

```

#### 系统配置

`jinfo <pid> `
 
或者指定属性配置

`jinfo <pid> | grep java.version`

#### command-line flags

**获取**
``` bash
$ jcmd 56227 VM.flags 
56227:
-XX:InitialHeapSize=268435456 -XX:MaxHeapSize=805306368 -XX:+UseCompressedOops -XX:+UseParallelGC 
```

``` bash
$ jinfo -flag MaxHeapSize 56227
-XX:MaxHeapSize=805306368
```

#### 参考

> 官方文档 [jinfo](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/jinfo.html)





# jmap


`jmap` 可以输出 Java 进程 内存中对象的工具，64位机上使用需要加上`-J-d64`参数。jmap 一般和 `jhat` 或者 `MAT`  配合使用，以图像的形式直观的展示当前内存是否有问题。

#### 参数说明
``` bash
-dump:[live,]format=b,file=<filename>
    以hprof二进制格式转储Java堆到指定filename的文件中。
    live子选项是可选的，如果指定了live子选项，堆中只有活动的对象会被转储。
    想要浏览heap dump，你可以使用 jhat(Java堆分析工具) 或者 MAT 读取生成的文件。
    
-finalizerinfo
    打印等待终结的对象信息。
    
-heap
    打印一个堆的摘要信息，包括使用的GC算法、堆配置信息和generation wise heap usage。
    
-histo[:live]
    打印每个Java类、对象数量、内存大小(单位：字节)、完全限定的类名。
    打印的虚拟机内部的类名称将会带有一个'*'前缀。
    如果指定了live子选项，则只计算活动的对象。
    
-permstat
    打印Java堆内存的永久保存区域的类加载器的智能统计信息。
    对于每个类加载器而言，它的名称、活跃度、地址、父类加载器、它所加载的类的数量和大小都会被打印。
    此外，包含的字符串数量和大小也会被打印。
    
-F
    强制模式。如果指定的pid没有响应，请使用jmap -dump或jmap -histo选项。此模式下，不支持live子选项。
    
-h | -help
    打印帮助信息。
    
-J<flag>
    指定传递给运行jmap的JVM的参数。
```



#### 例子


**jmap -histo `<pid>`**

打印每个Java类、对象数量、内存大小(单位：字节)、完全限定的类名。

``` bash
$ jmap -histo 56227

 num     #instances         #bytes  class name
----------------------------------------------
   1:         32283        4509704  <constMethodKlass>
   2:         32283        4139728  <methodKlass>
   3:           631        3899688  [I
   4:          3346        3563648  <constantPoolKlass>
   5:         11058        2885584  [B
   6:          3346        2262360  <instanceKlassKlass>
   7:          2569        1892992  <constantPoolCacheKlass>
   8:         13562        1296680  [C
   9:          3554         333488  java.lang.Class
  10:         11413         273912  java.lang.String
  11:          5401         268688  [[I
  12:          4874         257224  [S
  13:          5724         183168  java.util.concurrent.ConcurrentHashMap$HashEntry
  14:           427         178552  <methodDataKlass>
  15:          4108         164320  java.util.LinkedHashMap$Entry
  16:          1747         151976  [Ljava.util.HashMap$Entry;
...
1167:             1             16  com.sun.proxy.$Proxy27
1168:             1             16  java.util.logging.SimpleFormatter
Total        188585       28109352
```

> *B*  byte
> *C*  char
> *D*  double
> *F*  float
> *I*  int
> *J*  long
> *Z*  boolean
> *[*  数组，如[I表示int[]
> *[L+类名* 其他对象

**jmap -heap `<pid>`**

查看进程堆内存使用情况，包括使用的GC算法、堆配置参数和各代中堆内存使用情况

``` bash
$ jmap  -heap 56227
Attaching to process ID 56227, please wait...
Debugger attached successfully.
Server compiler detected.
JVM version is 24.75-b04

using thread-local object allocation.
Parallel GC with 4 thread(s)

Heap Configuration:
   MinHeapFreeRatio = 0
   MaxHeapFreeRatio = 100
   MaxHeapSize      = 805306368 (768.0MB)
   NewSize          = 1310720 (1.25MB)
   MaxNewSize       = 17592186044415 MB
   OldSize          = 5439488 (5.1875MB)
   NewRatio         = 2
   SurvivorRatio    = 8
   PermSize         = 21757952 (20.75MB)
   MaxPermSize      = 85983232 (82.0MB)
   G1HeapRegionSize = 0 (0.0MB)

Heap Usage:
PS Young Generation
Eden Space:
   capacity = 67633152 (64.5MB)
   used     = 8637816 (8.237663269042969MB)
   free     = 58995336 (56.26233673095703MB)
   12.771570959756541% used
From Space:
   capacity = 11010048 (10.5MB)
   used     = 0 (0.0MB)
   free     = 11010048 (10.5MB)
   0.0% used
To Space:
   capacity = 11010048 (10.5MB)
   used     = 0 (0.0MB)
   free     = 11010048 (10.5MB)
   0.0% used
PS Old Generation
   capacity = 71827456 (68.5MB)
   used     = 3938640 (3.7561798095703125MB)
   free     = 67888816 (64.74382019042969MB)
   5.483474174555201% used
PS Perm Generation
   capacity = 34078720 (32.5MB)
   used     = 17868280 (17.04051971435547MB)
   free     = 16210440 (15.459480285644531MB)
   52.43236835186298% used

4688 interned Strings occupying 380152 bytes.
```

其结果可和 `jstat -gc 56227` 一样，但是`jmap -heap 56227`要稍微详细一点
``` bash
$ jstat -gc 56227
 S0C    S1C    S0U    S1U      EC       EU        OC         OU       PC     PU    YGC     YGCT    FGC    FGCT     GCT   
10752.0 10752.0  0.0    0.0   66048.0   8435.4   70144.0     3846.3   33280.0 17449.5      5    0.046   4      0.193    0.239
```

**jmap -dump:format=b,file=`<dumpFileName>`  `<pid>`**

用jmap把进程内存使用情况dump到文件中
``` bash
jmap -dump:format=b,file=/tmp/123.hprof 56227 
Dumping heap to /Users/kail/Desktop/123.hprof ...
Heap dump file created
```
其与 `jcmd` dump 的结果是一样。
``` bash
jcmd 56227 GC.heap_dump /tmp/456.hprof
56227:
Heap dump file created
```
生成的文件内容是二进制，需要用 MAT 或者 jhat 工具打开进行分析。

以上是在JVM运行期 dump 出内存使用情况，如果需要在 JVM 崩溃的时候自动 dump，需要在启动的时候加上`-XX:+HeapDumpOnOutOfMemoryError`参数，加上`-XX:HeapDumpPath=<file_path>`指定保存位置




#### 参考

> [jmap](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/jmap.html)官方文档
>
> [jmap命令(Java Memory Map)](http://blog.csdn.net/fenglibing/article/details/6411953)
>
> [使用 Eclipse Memory Analyzer 进行堆转储文件分析](http://www.ibm.com/developerworks/cn/opensource/os-cn-ecl-ma/index.html)
>
> [Memory Analyzer (MAT)](http://www.eclipse.org/mat/)下载






# jhat

`jhat` 可以对 dump 出来的堆信息进行处理，以 html 页面的形式展示出来。

执行 `jhat /tmp/123.hprof`即可，默认端口是 `7000`，访问 `http://localhost:7000` 即可查看结果。

通过 `-port` 指定端口。

有时候文件过大的时候可能会出错，可以通过`-J-Xmx1024m`修改JVM最大可用内存。

其他参数详请查看官方文档。

#### 参考

> [jhat](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/jhat.html)官方文档
>
> [ jhat中的OQL（对象查询语言](http://blog.csdn.net/gtuu0123/article/details/6039592)





# jstack

`jstack` 可以打印出 线程的状态、调用栈、锁资源 等信息。 可以用于分析死锁、性能瓶颈等问题。用法相对简单但非常有用。

#### 参数

```` bash
-F          当 jstack [-l] <pid> 无相应的时候可以使用该参数强制dump

-l          打印关于锁的附加信息,例如属于java.util.concurrent的ownable synchronizers列表

-m          打印 java 和 native 代码的所有栈信息

-h|-help    打印帮助信息
````

#### 参考

> [jstack](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/jstack.html)官方文档
>
> [JAVA线程dump的分析 --- jstack pid](http://www.blogjava.net/jzone/articles/303979.html)






# jsadebugd

`jsadebugd`依附到一个Java进程，开启一个rmi服务，担当一个调试服务器的作用，可供 `jinfo`、`jmap`、`jstack` 命令拉取远程机器上的信息。

#### 开启服务

``` bash
$ jsadebugd 71069
Attaching to process ID 71069 and starting RMI services, please wait...
Debugger attached and RMI services started.
```
开启之后可以通过 `Ctrl + C` 关闭停止

#### 远程连接

``` bash
$ jinfo localhost
  
Attaching to remote server localhost, please wait...
Debugger attached successfully.
Server compiler detected.
JVM version is 24.75-b04
Java System Properties:
  
idea.version = =2016.3.3
java.runtime.name = Java(TM) SE Runtime Environment
java.vm.version = 24.75-b04
  
......

```
`jmap`、`jstack` 类似。


#### 参考
> [jsadebugd](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/jsadebugd.html)官方文档




# 参考

>[15 Troubleshooting](http://docs.oracle.com/javase/8/docs/technotes/tools/unix/s11-troubleshooting_tools.html)
>
>[JDK Tools and Utilities](http://docs.oracle.com/javase/8/docs/technotes/tools/)
>
>[Java Platform, Standard Edition (Java SE) 8](http://docs.oracle.com/javase/8/)
>
>[JDK命令](http://blog.csdn.net/gtuu0123/article/category/822015)
