---
title: Spring Loaded 热部署
date: 2016-08-06
---

在玩 JRebel 的时候发现了 Spring Loaded，相比JRebel来说，Spring Loaded最大的优势就是免费。
而且其挂在[spring-projects](https://github.com/spring-projects)下面，也算是有个强大的后盾。

<!--more-->

实际上在Debug模式下，Java是支持部分的热部署能力的，但只限于修改方法体，如果新增的方法或者删除了方法就无能为力了。

Spring Loaded 可以解决上面的问题，新增、删除、修改 （方法|构造函数|变量|枚举），都可以实现热部署。美中不足是无法实现对配置的热部署。

使用方法也很简单 直接在启动时加上以下JVM参数即可。

```
-javaagent:{path}/springloaded-{VERSION}.jar -noverify
```

springloaded 仓库地址：http://repo.spring.io/release/org/springframework/springloaded/

当前最新版本是[springloaded-1.2.6.RELEASE.jar](http://repo.spring.io/release/org/springframework/springloaded/1.2.6.RELEASE/springloaded-1.2.6.RELEASE.jar)，点击[下载](http://repo.spring.io/release/org/springframework/springloaded/1.2.6.RELEASE/springloaded-1.2.6.RELEASE.jar)即可。

> spring-loaded github： https://github.com/spring-projects/spring-loaded


##### 扫描范围

Spring Loaded默认对 Jar包 和 以下包中的类变化不进行扫描
```
antlr/
org/springsource/loaded/
com/springsource/tcserver/
com/springsource/insight/
groovy/
groovyjarjarantlr/
groovyjarjarasm/
grails/
java/
javassist/
org/codehaus/groovy/
org/apache/
org/springframework/
org/hibernate/
org/hsqldb/
org/aspectj/
org/xml/
org/h2/
```

> spring-loaded Wiki: https://github.com/spring-projects/spring-loaded/wiki/Basic-usage-information

##### 其他参数

```
-Dspringloaded=verbose
```
`verbose` - Spring Loaded 会详细的打印出 后台处理器都干了些什么，哪些文件发生了变化等。

```
-Dspringloaded=explain
```
`explain` - Spring Loaded 会解释为什么会做某些决定，例如会告诉你为什么你的类不能为loaded，可能是因为写在了`org.springframework`包下面 等。

##### 详见官网 WiKi :
> Configuration-Options Wiki: https://github.com/spring-projects/spring-loaded/wiki/Configuration-Options
```
-Dspringloaded=verbose;explain;profile=grails
```