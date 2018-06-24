---
title: JDK1.8.0_151的无限制强度加密策略文件变动
tags:
  - Java
categories:
  - Java
date: 2018-06-23
---

随着越来越多的第三方工具只支持 JDK8，最近公司也计划从 JDK7 升级到 JDK8，在线下环境升级过程中，发现某些项目报以下异常：
``` java
java.security.InvalidKeyException: Illegal key size
```
这是因为某些国家的进口管制限制，JDK默认的加解密有一定的限制。
比如默认不允许 256 位密钥的 AES 加解密，解决方法就下载官方**JCE无限制强度加密策略文件**，覆盖即可。

但是发现公司选用的 **1.8.0_151** 版本的 `$JAVA_HOME/jre/lib/security/` 目录下面多了一个 `policy` 文件夹，里面还有两个文件夹 
``` bash
├── limited
│   ├── local_policy.jar
│   └── US_export_policy.jar
└── unlimited
    ├── local_policy.jar
    └── US_export_policy.jar
```
于是搜了一下，发现了这篇文章：[Java Unlimited Strength Crypto Policy for Java 9 or 1.8.0_151](https://www.petefreitag.com/item/844.cfm)，以下内容为该文章的翻译。


<!-- more -->

从Java 1.8.0_151和1.8.0_152开始，为JVM启用 无限制强度管辖策略 有了一种新的更简单的方法。如果不启用此功能，则不能使用AES-256。

首先下载JRE，我喜欢使用`server-jre`作为服务器。
当您解压`server-jre`时，请在 `jre/lib/security` 文件夹中查找文件 `java.security`。
例如，对于`Java 1.8.0_152`，文件结构如下所示:

``` bash
/jdk1.8.0_152
   |- /jre
        |- /lib
              |- /security
                    |- java.security
```


现在用文本编辑器打开`java.security`，并找到定义java安全性属性`crypto.policy`的行，它可以有两个值`limited`或`unlimited` - 默认值是`limited`。

默认情况下，您应该能找到一条注释掉的行：
``` bash
#crypto.policy=unlimited
```

您可以通过取消注释该行来启用无限制，删除`＃`：

``` bash
crypto.policy=unlimited
```

现在重新启动指向JVM的Java应用程序即可。


# Read More

- [Java Unlimited Strength Crypto Policy for Java 9 or 1.8.0_151](https://www.petefreitag.com/item/844.cfm)
- [JCE policy changes in Java SE 8u151, 8u152 and 8u162](https://golb.hplar.ch/2017/10/JCE-policy-changes-in-Java-SE-8u151-and-8u152.html)
- [AES的256位密钥加解密报 java.security.InvalidKeyException: Illegal key size or default parameters 异常的处理及处理工具](https://blog.csdn.net/dafeige8/article/details/76019911)

