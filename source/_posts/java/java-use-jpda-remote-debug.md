---
title: Java JPDA 远程调试
date: 2017-02-17
desc: JPDA,远程调试,IDEA,Tomcat,Resin

tags: [JPDA,debug,jcmd,jps,jinfo]
categories: Java
---

代码调试最简单的办法就是输出日志的方式，但是如果代码发布在远程机器上，通过输出日志的方式可能不太高效，需要频繁的发布代码。本文会通过简单的配置实现**远程代码断点调试**。

JPDA（Java Platform Debugger Architecture）是 Java 平台调试体系结构的缩写，通过 JPDA 提供的 API，开发人员可以方便灵活的搭建 Java 调试应用程序。

<!--more-->


# 服务器应用 JVM 启动参数

实现远程断点调试，需要在远程机器上，启动 Java 应用的时候加上以下启动参数
``` bash
-Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n 
```
具体含义如下
``` 
-Xrunjdwp                   # 启用JDWP实现，它包含若干子选项：
    transport=dt_socket     # JPDA front-end和back-end之间的传输方法。dt_socket表示使用套接字传输。
    address=8000            # JVM在8000端口上监听请求。
    server=y                # y表示启动的JVM是被调试者。如果为n，则表示启动的JVM是调试器。
    suspend=n               # y表示启动的JVM会暂停等待，直到调试器连接上，n表示不暂停等待
```

## Tomcat

编辑 `bin/catalina.sh`(`bin/catalina.bat`)，查找字符串 `JAVA_OPTS`、`org.apache.catalina.startup.Bootstrap` 或 `JPDA_OPTS`，找到启动的位置，加上以上启动参数即可。

## Resin

编辑 `conf/resin.properties`，查找字符串 `jvm_args`(如果是注释掉的话就打开)，新增以上参数，用空格分割，如下（`-Xmx2048m -XX:MaxPermSize=256m`是默认的）：
```
jvm_args  : -Xmx2048m -XX:MaxPermSize=256m -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n 
```

## 在远程机器上查看JVM启动参数是否配置成功

通过 `jcmd` 或者 `jps` 找到进程 ID (pid)，然后通过 `jcmd <pid> VM.command_line` 或者  `jinfo -flags <pid>` 查看启动参数里面是否包含以上配置。

> [Java 内建"故障排除"工具 — jcmd、jinfo、jmap、jhat、jstack、jsadebugd](/post/2017-02-10-java-self-troubleshooting-command.html)








# IDE 配置

## IDEA

菜单 `>` Run `>` Edit Configurations... `>` + (快捷键 `Alt + Insert`) `>` Remote `>` 填写 Host 和 port并选择被调试的项目，运行即可。


IDEA 比较人性化，各 JDK 版本的配置已经写好了，直接复制配置到远程服务端即可，如下供参考：

```
# JDK 1.5 +
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000
  
# JDK 1.4 +
-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000
  
# JDK 1.3 或 更早
-Xnoagent -Djava.compiler=NONE -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000
```

## Eclipse

**Debug Configurations** `>` Remote Java Application `>` 填写 Host 和 port并选择被调试的项目，运行即可。





# 调试

调试的时候在具体代码处打上断点即可，**要注意的是：服务器端代码和本地代码要保持一致**。
    

# 拓展阅读
> [Java Platform Debugger Architecture (JPDA)](http://docs.oracle.com/javase/8/docs/technotes/guides/jpda/index.html)
>
> [深入 Java 调试体系: 第 1 部分，JPDA 体系概览](https://www.ibm.com/developerworks/cn/java/j-lo-jpda1/index.html)
> 
> [深入 Java 调试体系，第 2 部分: JVMTI 和 Agent 实现](https://www.ibm.com/developerworks/cn/java/j-lo-jpda2/index.html)
> 
> [深入 Java 调试体系，第 3 部分: JDWP 协议及实现](https://www.ibm.com/developerworks/cn/java/j-lo-jpda3/index.html)
> 
> [深入 Java 调试体系，第 4 部分: Java 调试接口（JDI）](https://www.ibm.com/developerworks/cn/java/j-lo-jpda4/index.html)
