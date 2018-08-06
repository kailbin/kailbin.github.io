---
title: Jsonde
tags:
  - Java
  - Tuning
categories:
  - Tools
date: 2018-08-06
id: jsonde
---



**jSonde**  采用 GPL v3 开源协议，是 **jTracert** 的变种升级，拥有更强的统计分析功能，能够帮助用户在Java程序运行的过程中动态的生成 UML序列图、调用关系、调用耗时 等功能的 **客户端工具**。

<!-- more -->

# jSonde 的主要功能

- **Sequence Diagram**：方法调用关系 UML序列图
- Profiler
  - **Method Call**： 方法调用关系 和 耗时
  - **Heap**： 类的实例个数 和 总大小
  - Memory Telemetry： 内存监控
  - Class Telemetry： Class 数监控
- Reports
  - Top Methods Throwing Exception： 抛出异常最多的前几个方法
  - **Top Code Sources By Execution Time**： 耗时最长的前几个类库
  - **Dependencies** ： jar 依赖关系



# 使用方式

## 获取资源

- Github：[bedrin/jsonde](https://github.com/bedrin/jsonde)
- GitPage: https://bedrin.github.io/jsonde/
- 下载 ： [jsonde-1.1.0-installer.jar](https://github.com/bedrin/jsonde/releases/download/1.1.0/jsonde-1.1.0-installer.jar) 

## 安装

```java
java -jar jsonde-1.1.0-installer.jar
```

## 添加 jsonde agent

被监控程序 新增 JVM 参数

```
-javaagent:%JSONDE_HOME%/lib/jsonde.agent-1.1.0.jar=60001
```

- `JSONDE_HOME` 是 jsonde 的安装目录
- `60001` 是开启的监控端口
- **启动被监控应用后控制台会输出 `jSonde agent started ` 并夯住，直到 GUI 程序链接到设定的端口才会开始启动**

## 启动 GUI 程序

- `java -jar %JSONDE_HOME%/jsonde.jar` 启动，可以通过其提供的 可执行文件 或 脚本启动
- `File > New Project` 新建工程，输入工程名 和 需要建空间的包 如 `xyz.kail.*`
- `Connect` 链接，*这时候被监控程序正式开始启动*。

# 小结

> jSonde allows you to generate sequence diagrams directly from your application runtime! This gives you a lot of advantages:
>
> - Understand the code created by your colleagues/partners in a short time
> - Rapidly generate documentation for your partners or users.
> - Easily investigate what's happening in large Java applications
> - Excellent companion for a common debugger

jSonde 声称有以下好处:

- 快速了解同事写的代码（： 感觉这个程序使用起来相当卡，尤其是调用关系复杂，调用比较深的情况下）
- 为同事或者用户快速生成文档（： 可以生成 关系图，貌似不能生成文档）
- 轻松查看大型Java应用程序中发生什么（：耗时、调用关系、依赖关系）
- 是一个调试伴侣

个人感觉

- 程序启动后会夯住，这个特性可以用来**查看调试启动耗时**
- 调用指定接口或者方法的时候才会生成关系，可以用来**熟悉框架的调用的关系*和流程*
- 其他监控功能不如 JDK 自带的一些调优工具好用
- 该工具只适合开发调试阶段使用

# Read More

## 官网

- [Jsonde](https://bedrin.github.io/jsonde/)

## 其它

- [JSONDE 介绍](https://www.xuebuyuan.com/zh-tw/861640.html)
- [ jSonde-Java逆向工程、监控汇总工具](https://blog.csdn.net/davyxie/article/details/5646715)
- Java开源分类 > 运行分析工具 > [jSonde 简介信息](http://www.open-open.com/open266259.htm)