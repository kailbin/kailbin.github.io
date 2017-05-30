---
title: 在 Java 应用中整合 Groovy
date: 2017-05-30
desc: 在 Java 应用中整合 Groovy

tags: [Groovy,Java]
categories: Java
---

> The Groovy language proposes several ways to integrate itself into applications (Java or even Groovy) at runtime, from the most basic, simple code execution to the most complete, integrating caching and compiler customization

Groovy语言提出了几种在运行时将自身集成到应用程序(Java甚至Groovy)的方法，从最基本的简单代码执行到最完整的、集成缓存和编译器定制。

这里介绍了几种 Java 集成 Groovy 的几种方式。

<!--more-->

# 准备

首先在项目中添加 Maven 依赖

``` xml
<dependency>
    <groupId>org.codehaus.groovy</groupId>
    <artifactId>groovy-all</artifactId>
    <version>2.4.11</version>
</dependency>
```

# Eval

`groovy.util.Eval` 类是在运行时动态地执行Groovy脚本的最简单的方法。可以通过调用me方法来完成:

``` Java
// 输出 99
System.out.println(Eval.me("33 * 3 "));
// 输出 KAIL  == "kail".toUpperCase()
System.out.println(Eval.me("\"kail\".toUpperCase()"));
// 输出 Hello World  == String.format("Hello %s","World")
System.out.println(Eval.me("String.format(\"Hello %s\",\"World\")"));
```

# GroovyShell

``` java
GroovyShell shell = new GroovyShell();

// 1. 求值，输出2 ， 是 Eval 的内部实现
System.out.println(shell.evaluate("\"Kail\".indexOf('i')"));

// 2. 输出 6
Script script = shell.parse("3 * (5 - 3)");
System.out.println(script.run());

// 3. 绑定参数
// groovy printlv 输出 2017-05-30  
// java System.out.println 输出  name: Kail, data: Tue May 30 16:03:24 CST 2017 !
shell.setProperty("name", "Kail");
shell.setProperty("date", new Date());
System.out.println(shell.evaluate("println new java.text.SimpleDateFormat('yyyy-MM-dd').format(date); \"name: $name, data: $date !\" "));

// 4. 执行 .groovy 文件，修改文件不会生效
Script script1 = shell.parse(new File("/Users/kail/_test/test.groovy"));
for (int i = 0; i < 20; i++) {
    script1.run();
    Thread.sleep(1000);
}

// 5. 执行 .groovy 文件，修改文件后立马会生效
for (int i = 0; i < 20; i++) {
    shell.evaluate(new File("/Users/kail/_test/test.groovy"));
    Thread.sleep(1000);
}
```
test.groovy 内容如下，只是简单地输出一句话

``` grovvy
println 12345
```

## 需要注意的事
拓展阅读中（*在Java里整合Groovy脚本的一个陷阱*）提到了集成Groovy脚本可能会出现`内存溢出`的问题，这个问题在高版本中貌似已经被修复了，本文在测试的时候并没有发现。
虽然没有显式的报错，但是GC还是很厉害，最直观的感受就是运行了一段时间，控制台输出会卡顿一会。
这里建议一次编译（`shell.parse(file)`）多次使用(或者使用 `GroovyScriptEngine`)，本地缓存编译结果，定时扫描 `.groovy` 文件是否有变动，有变动再重新编译。

# GroovyClassLoader

GroovyShell是一个执行Groovy脚本的简单工具。在内部它使用了 `groovy.lang.GroovyClassLoader`，它是编译和在运行时加载类的核心。先上一个示例

``` java
GroovyClassLoader gcl = new GroovyClassLoader();
Class<?> fooClass = gcl.parseClass("class Foo { void doIt() { println 'ok' } }");
Object object = fooClass.newInstance();

// 输出 Foo
System.out.println(fooClass.getName());
// 打印 ok
fooClass.getMethod("doIt").invoke(object);
```

## 缓存相关

``` java
GroovyClassLoader gcl = new GroovyClassLoader();
Class<?> fooClass1 = gcl.parseClass("class Foo { void doIt() { println 'ok' } }");
Class<?> fooClass2 = gcl.parseClass("class Foo { void doIt() { println 'ok' } }");
System.out.println(fooClass1.getName()); // 输出 Foo
System.out.println(fooClass2.getName()); // 输出 Foo
System.out.println(fooClass1 == fooClass2); // 输出false

String path = "/Users/kail/_test/test.groovy";
Class<?> fooClass3 = gcl.parseClass(new File(path));
Class<?> fooClass4 = gcl.parseClass(new File(path));

System.out.println(fooClass3.getName()); // 输出 test
System.out.println(fooClass4.getName()); // 输出 test
System.out.println(fooClass3 == fooClass4); // 输出 true
```
从上面例子可以看出，从字符串解析类和从文件解析是不一样的。
**使用文件作为输入，GroovyClassLoader能够缓存生成的类文件，避免在运行时为相同的 Groovy来源 创建多个类。**

# GroovyScriptEngine

`GroovyScriptEngine`与`GroovyShell` 类似，其最大的不同在于`GroovyScriptEngine`会监控文件的变化，例如下面的例子，修改 test.groovy 保存之后会实时生效。

``` java
GroovyScriptEngine scriptEngine = new GroovyScriptEngine("/Users/kail/_test/");

for (; ; ) {
    scriptEngine.run("test.groovy", "");
    Thread.sleep(1000);
}
```






# 拓展阅读

> [Integrating Groovy in a Java application](http://docs.groovy-lang.org/docs/latest/html/documentation/#_integrating_groovy_in_a_java_application)
>
> [在Java里整合Groovy脚本的一个陷阱](http://rednaxelafx.iteye.com/blog/620155)



