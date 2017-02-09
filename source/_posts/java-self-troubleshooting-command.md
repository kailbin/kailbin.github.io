---
title: Java 内建"故障排除"工具 — jcmd、jinfo、jhat、jmap、jsadebugd、jstack
date: 2000-02-10
desc: jcmd,jinfo,jhat,jmap,jsadebugd,jstack
---

jcmd: 向 JVM 发送诊断命令

jinfo: 获得 JVM 配置信息

jmap: 打印Java进程的 堆、核心文件、远程调试服务 信息

jhat: 分析 Java 堆`heap` 的工具

jstack: 打印Java进程的 栈、核心文件、远程调试服务 信息

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




# jmap: 打印Java进程的 堆、核心文件、远程调试服务 信息

# jhat: 分析 Java 堆`heap` 的工具

# jstack: 打印Java进程的 栈、核心文件、远程调试服务 信息

# jsadebugd: 依附到一个Java进程或核心文件，担当一个调试服务器的作用


# 参考

>[15 Troubleshooting](http://docs.oracle.com/javase/8/docs/technotes/tools/unix/s11-troubleshooting_tools.html)
>
>[JDK Tools and Utilities](http://docs.oracle.com/javase/8/docs/technotes/tools/)
>
>[Java Platform, Standard Edition (Java SE) 8](http://docs.oracle.com/javase/8/)
