---
title: javassist
date: 2017-05-28
desc: javassist

tags: [javassist]
categories: Java
---

Javassist 是一个操作 Java 字节码的类库，通过它可以直接操作 Java 的 `.class` 字节码文件。


<!--more-->

# 读写字节码

`Javassist.CtClass` 是类文件的抽象。 `CtClass`（compile-time class）对象负责处理一个类文件 。下面是个简单的例子：
``` java
ClassPool pool = ClassPool.getDefault();
CtClass cc = pool.get("test.Rectangle");
cc.setSuperclass(pool.get("test.Point"));
cc.writeFile();
```

### 参考
>
>[Javassist 官方文档](http://jboss-javassist.github.io/javassist/)  
>
