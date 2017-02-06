---
title: Java 内建"监控"工具
date: 2000-02-10
desc: java,jps,jstat,jstatd,jmc
---

jps: 列出当前系统的Java进程
jstat: 显示 JVM 统计信息
jstatd: 可以远程监控 JVM 统计信息
jmc: 启动 Java Mission Controla 工具， 监控和管理正在运行的Java应用或者JVMs。

<!--more-->

# jps 列出当前系统的Java进程

语法： 
`jps [ options ] [ hostid ]`

`jps [-q] [-mlvV] [<hostid>]`    

#### options
``` bash
-help       帮助信息
-q          只列出进程ID
-m          显示main方法参数
-l          显示main方法包名 或者 应用jar包的全路径
-v          显示JVM参数
-V          通过 flags 文件传递给JVM的参数 (the .hotspotrc file or the file specified by the -XX:Flags=<filename> argument)
-J          给jps命令传递JVM参数，例如：jps -J-Xms2m 分配了 2M 起始内存
```

#### hostid

// TODO jstatd


#### 官方文档

jps - Java Virtual Machine Process Status Tool [JDK7](https://docs.oracle.com/javase/7/docs/technotes/tools/share/jps.html)/[JDK8]((http://docs.oracle.com/javase/8/docs/technotes/tools/windows/jps.html)

JDK7 和 JDK8 对 jps 的解释还是稍有不同的。  


# jstat 显示 JVM 统计信息

这个给出一个图供参考，应该来自《深入理解Java虚拟机》这本书
![JVM内存分布](/images/java-self-monitor-command/1.png)


[jstat - Java Virtual Machine Statistics Monitoring Tool](https://docs.oracle.com/javase/7/docs/technotes/tools/share/jstat.html)

# jstatd 


# jmc

[Java Mission Control User's Guide](https://docs.oracle.com/javacomponents/jmc-5-5/jmc-user-guide/toc.htm)  
[Java Flight Recorder Runtime Guide](https://docs.oracle.com/javacomponents/jmc-5-5/jfr-runtime-guide/toc.htm)  


# 参考

>[13 Monitor the JVM](http://docs.oracle.com/javase/8/docs/technotes/tools/unix/s9-monitoring-tools.html)
>
>[JDK Tools and Utilities](http://docs.oracle.com/javase/8/docs/technotes/tools/)
>
>[Java Platform, Standard Edition (Java SE) 8](http://docs.oracle.com/javase/8/)
>
>[Java垃圾回收机制](http://doc.redisfans.com)
>
>[Jstatd命令(Java Statistics Monitoring Daemon)](http://blog.csdn.net/fenglibing/article/details/17323515)
