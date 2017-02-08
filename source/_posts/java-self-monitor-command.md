---
title: Java 内建"监控"工具 — jps、jstat、jstatd
date: 2017-02-07
desc: java,jps,jstat,jstatd,jmc,远程监控,Java进程
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

可以进行远程监控，详请查看 ** [jstatd 例子](#jstatd)部分 **


#### 官方文档

jps - Java Virtual Machine Process Status Tool [JDK7](https://docs.oracle.com/javase/7/docs/technotes/tools/share/jps.html)/[JDK8]((http://docs.oracle.com/javase/8/docs/technotes/tools/windows/jps.html)

JDK7 和 JDK8 对 jps 的解释还是稍有不同的。  









# jstat 显示 JVM 统计信息

### 一般参数

``` bash
-help       显示帮助信息
-options    支持的统计项
```
这里使用`-options` 输出如下：

``` bash
$ jstat -options
-class
-compiler
-gc
-gccapacity
-gccause
-gcnew
-gcnewcapacity
-gcold
-gcoldcapacity
-gcpermcapacity
-gcutil
-printcompilation
```

### 输出参数

格式 ： `jstat -<option> [-t] [-h<lines>] <vmid> [<interval> [<count>]]`

``` bash
<interval>          每隔多长时间统计一次，单位支持 ms 和 s
<count>             统计的次数
-t                  每次会输出目标JVM启动开始到现在的时间戳
-h<lines>           没隔多少次输出一下统计表头
```

### 例子

``` bash
$ jstat -gc -t -h 4 20901 500 10
Timestamp        S0C    S1C    S0U    S1U      EC       EU        OC         OU       PC     PU    YGC     YGCT    FGC    FGCT     GCT   
         1998.6 10752.0 10752.0 8038.9  0.0   66048.0  45065.2   174592.0    627.8    21504.0 17411.2      2    0.039   1      0.029    0.069
         1999.2 10752.0 10752.0 8038.9  0.0   66048.0  45065.2   174592.0    627.8    21504.0 17411.2      2    0.039   1      0.029    0.069
         1999.6 10752.0 10752.0 8038.9  0.0   66048.0  45065.2   174592.0    627.8    21504.0 17411.2      2    0.039   1      0.029    0.069
         2000.2 10752.0 10752.0 8038.9  0.0   66048.0  45065.2   174592.0    627.8    21504.0 17411.2      2    0.039   1      0.029    0.069
Timestamp        S0C    S1C    S0U    S1U      EC       EU        OC         OU       PC     PU    YGC     YGCT    FGC    FGCT     GCT   
         2000.7 10752.0 10752.0 8038.9  0.0   66048.0  45065.2   174592.0    627.8    21504.0 17411.2      2    0.039   1      0.029    0.069
         2001.2 10752.0 10752.0 8038.9  0.0   66048.0  45065.2   174592.0    627.8    21504.0 17411.2      2    0.039   1      0.029    0.069
         2001.7 10752.0 10752.0 8038.9  0.0   66048.0  45065.2   174592.0    627.8    21504.0 17411.2      2    0.039   1      0.029    0.069
         2002.2 10752.0 10752.0 8038.9  0.0   66048.0  45065.2   174592.0    627.8    21504.0 17411.2      2    0.039   1      0.029    0.069
Timestamp        S0C    S1C    S0U    S1U      EC       EU        OC         OU       PC     PU    YGC     YGCT    FGC    FGCT     GCT   
         2002.7 10752.0 10752.0 8038.9  0.0   66048.0  45065.2   174592.0    627.8    21504.0 17411.2      2    0.039   1      0.029    0.069
         2003.2 10752.0 10752.0 8038.9  0.0   66048.0  45065.2   174592.0    627.8    21504.0 17411.2      2    0.039   1      0.029    0.069
```

##### 解释
```
-gc   ：JVM中堆的垃圾收集情况的统计
-t    ：即第一列 Timestamp 这一列
-h 4  ：每输出4次，就把表头再输出一次
20901 ：vmid，JVM 的进程ID
500   ：每500毫秒输出一次统计
10    ：统计10次
```

如果要看懂以上每一列的含义，其涉及到 JVM 分代GC的问题，这里给出一个图供参考，应该来自《深入理解Java虚拟机》这本书。

![JVM内存分布](/images/java-self-monitor-command/1.png)

以下给出简单解释：

> 堆内存 = 年轻代 + 年老代 + 永久代  
> 年轻代 = Eden区 + 两个Survivor区（From和To）  
> 
> S0C、S1C、S0U、S1U： Survivor 0/1区容量（Capacity）和使用量（Used）  
> EC、EU：            Eden区容量和使用量  
> OC、OU：            年老代容量和使用量  
> PC、PU：            永久代容量和使用量   
> YGC、YGT：          年轻代GC次数和GC耗时  
> FGC、FGCT：         Full GC次数和Full GC耗时  
> GCT：              GC总耗时

>> [Java垃圾回收机制](http://www.jianshu.com/p/778dd3848196)
>> [HotSpot Virtual Machine Garbage Collection Tuning Guide](http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/toc.html)

详细解释详见[官网 jstat](https://docs.oracle.com/javase/7/docs/technotes/tools/share/jstat.html#output_options)

网友总结的 [jstat命令(Java Virtual Machine Statistics Monitoring Tool)](http://blog.csdn.net/fenglibing/article/details/6411951)

摘录
``` 
gc                  用于查看JVM中堆的垃圾收集情况的统计
gcutil              用于查看新生代、老生代及持代垃圾收集的情况
class               用于查看类加载情况的统计
compiler            用于查看HotSpot中即时编译器编译情况的统计
gccapacity  	    用于查看新生代、老生代及持久代的存储容量情况
gccause	            用于查看垃圾收集的统计情况（这个和-gcutil选项一样），如果有发生垃圾收集，它还会显示最后一次及当前正在发生垃圾收集的原因。
gcnew               用于查看新生代垃圾收集的情况
gcnewcapacity	    用于查看新生代的存储容量情况
gcold	            用于查看老生代及持久代发生GC的情况
gcoldcapacity	    用于查看老生代的容量
gcpermcapacity	    用于查看持久代的容量
printcompilation    HotSpot编译方法的统计
```





# jstatd 

jstatd 会开启一个 RMI 服务，供其他机器进行远程监控

#### 参数

``` bash
-nr     如果RMI注册中心没有找到，不会创建一个内部的RMI注册中心
-p      RMI端口，默认为1099
-n      RMI名称， 默认是 JStatRemoteHost
-J      传递JVM参数
```

#### 例子

**文件 `jstatd.all.policy`内容如下，给jstatd授予所有权限**
``` 
grant codebase "file:${java.home}/../lib/tools.jar" {   
    permission java.security.AllPermission;
};
```

**开启jstatd 守护进程**，开启后会有一个光标一直在闪，`Ctrl + C` 退出
``` bash
jstatd -J-Djava.security.policy=jstatd.all.policy
```

**绑定端口和设置名称**
``` bash
jstatd -J-Djava.security.policy=jstatd.all.policy -p 6789 -n rmiJstatsName
```

**使用jps远程监控**
假设 jstatd 的所在机器是 `192.168.4.35`
``` bash
jps rmi://192.168.4.35:6789/rmiJstatsName
```
输出结果

    11452 RemoteMavenServer
    4440 Jstatd
    10016 Launcher

**使用 jstat 远程监控**
``` bash
jstat -gcutil 11452@192.168.4.35:6789/rmiJstatsName 500 10
```

**打开日志输出**
``` bash
jstatd -J-Djava.security.policy=jstatd.all.policy -J-Djava.rmi.server.logCalls=true
```

### 详请查看以下文档

> 官网 [jstatd](http://docs.oracle.com/javase/8/docs/technotes/tools/unix/jstatd.html)
>
> [jstatd 命令(Java Statistics Monitoring Daemon) ](http://blog.csdn.net/fenglibing/article/details/17323515)
>
> [Default Policy Implementation and Policy File Syntax](http://docs.oracle.com/javase/8/docs/technotes/guides/security/PolicyFiles.html)







# jmc

jmc (Java Mission Control) 是一个集成的图形化界面管理工具，监控功能十分强大，下面是官方文档，这里暂不做过多介绍。 

[Java Mission Control User's Guide](https://docs.oracle.com/javacomponents/jmc-5-5/jmc-user-guide/toc.htm)  
[Java Flight Recorder Runtime Guide](https://docs.oracle.com/javacomponents/jmc-5-5/jfr-runtime-guide/toc.htm)  









# 参考

>[13 Monitor the JVM](http://docs.oracle.com/javase/8/docs/technotes/tools/unix/s9-monitoring-tools.html)
>
>[JDK Tools and Utilities](http://docs.oracle.com/javase/8/docs/technotes/tools/)
>
>[Java Platform, Standard Edition (Java SE) 8](http://docs.oracle.com/javase/8/)
>
>[JDK内置工具使用](http://blog.csdn.net/fenglibing/article/details/6411999)
