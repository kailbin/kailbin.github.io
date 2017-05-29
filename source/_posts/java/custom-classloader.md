---
title: 自定义 ClassLoader
date: 2017-05-29
desc: 自定义 ClassLoader

tags: [Java,ClassLoader]
categories: Java
---

`ClassLoader`（类加载器）是负责加载class文件到JVM的抽象类。
扩展 Java 虚拟机动态加载类的方式，需要继承 `ClassLoader`，并重写`findClass()`或`loadClass()`等方法。

<!--more-->

# `findClass()`和`loadClass()` 关系

`findClass()` 是一个没有没有实质内容的方法，其方法体直接抛出`ClassNotFoundException`异常，访问类型是 `protected`，其目的就是为了被重写，重写`findClass()`方法也是官方推荐的方式。`findClass()`的源码如下：
``` java
/*
 * 使用指定的 二进制名称(全路径名)查找类。此方法应该被类加载器的实现重写，
 * 该实现按照委托模型（双亲委派）来加载类。在通过父类加载器检查所请求的类后，
 * 此方法将被 loadClass 方法调用。默认实现抛出一个 ClassNotFoundException。
 *
 * @param  name                       类的 二进制名称（全路径名）
 * @return                            The resulting <tt>Class</tt> object
 * @throws  ClassNotFoundException    If the class could not be found
 *
 * @since  1.2
 */
protected Class<?> findClass(String name) throws ClassNotFoundException {
    throw new ClassNotFoundException(name);
}
```
注释摘录自 JDK1.6 中文版文档。 从注释中可以看出，`loadClass()`会调用`findClass()`来查找类。`loadClass()`源码摘录如下：

``` java
protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    // 加锁
    synchronized (getClassLoadingLock(name)) {
        // 首先检查类是否已经被加载，该方法跟踪到最后是一个 native 方法
        Class<?> c = findLoadedClass(name);
        // 如果没有找到
        if (c == null) {
            try { 
                if (parent != null) {
                    // 如果没有找到，先从父加载器进行加载
                    c = parent.loadClass(name, false);
                } else {
                    // 如果没有父加载器就从 Bootstrap 加载器找，跟踪到最后是一个 native 方法
                    c = findBootstrapClassOrNull(name);
                }
            } catch (ClassNotFoundException e) {
                // ClassNotFoundException thrown if class not found
                // from the non-null parent class loader
            }

            if (c == null) {
                ......
                // 如果都没找到就调用 findClass。这里用户可以自定义从哪里加载 class 文件
                c = findClass(name);

                ......
            }
        }
        ......

        return c;
    }
}
```


# 双亲委派

谈 `ClassLoader` 必备的一个概念就是 **双亲委派**。
如上面`loadClass()`源码所示，加载一个类，先问 父加载器 有没有，类似于这种形式，人们起了一个高大上的名字叫 **双亲委派**。
需要注意的是 父子之间并不是继承的关系，而是组合关系。创建 ClassLoader 的时候可以传入 parent ClassLoader。

下面是一个测试：
``` Java
// 输出 sun.misc.Launcher$AppClassLoader@18b4aac2
System.out.println(Thread.currentThread().getContextClassLoader());
// 输出 sun.misc.Launcher$ExtClassLoader@5ca881b5
System.out.println(Thread.currentThread().getContextClassLoader().getParent());
// 输出 null
System.out.println(Thread.currentThread().getContextClassLoader().getParent().getParent());

// 输出 sun.misc.Launcher$ExtClassLoader@5ca881b5
System.out.println(com.sun.nio.zipfs.ZipInfo.class.getClassLoader());

// 输出 null
System.out.println(String.class.getClassLoader());
```
可以看出，当前线程的 ClassLoader 是 `sun.misc.Launcher$AppClassLoader`，其父是`sun.misc.Launcher$ExtClassLoader`，其祖是 null。
`com.sun.nio.zipfs.ZipInfo` 的 ClassLoader 直接就是 `sun.misc.Launcher$ExtClassLoader`。
`String` 的 ClassLoader 是 null。

以下给出解释

> `BootStrap ClassLoader`：启动类加载器，负责加载存放在`%JAVA_HOME%\lib`目录中的，或者通被`-Xbootclasspath`参数所指定的路径中的，并且被java虚拟机识别的(仅按照文件名识别，如rt.jar，名字不符合的类库，即使放在指定路径中也不会被加载)类库到虚拟机的内存中，启动类加载器无法被java程序直接引用。
>
> `Extension ClassLoader`：扩展类加载器，由`sun.misc.Launcher$ExtClassLoader`实现，负责加载`%JAVA_HOME%\lib\ext`目录中的，或者被`java.ext.dirs`系统变量所指定的路径中的所有类库，开发者可以直接使用扩展类加载器。

> `Application ClassLoader`：应用程序类加载器，由`sun.misc.Launcher$AppClassLoader`实现，负责加载用户类路径classpath上所指定的类库，是类加载器ClassLoader中的getSystemClassLoader()方法的返回值，开发者可以直接使用应用程序类加载器，如果程序中没有自定义过类加载器，该加载器就是程序中默认的类加载器。



# 重写 `findClass()`

``` java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MyClassLoader extends ClassLoader {

    private static final String MY_CLASS_PATH = "/Users/kail/_test";

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] allBytes = Files.readAllBytes(Paths.get(MY_CLASS_PATH, name.replace(".", "/") + ".class"));
            return defineClass(name, allBytes, 0, allBytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

}
```
这里使用上篇 [Javassit](/post/2017-05-29/java/make-javassist-simple.html) 生成的 `/Users/kail/_test/xyz/kail/blog/CodeClass.class` 文件。

```java
MyClassLoader myClassLoader = new MyClassLoader();

// class xyz.kail.blog.CodeClass
System.out.println(Class.forName("xyz.kail.blog.CodeClass", true, myClassLoader));

//sun.misc.Launcher$AppClassLoader@18b4aac2
System.out.println(Thread.currentThread().getContextClassLoader()); 

Thread.currentThread().setContextClassLoader(myClassLoader);
//classloader.MyClassLoader@24d46ca6
System.out.println(Thread.currentThread().getContextClassLoader()); 

//class xyz.kail.blog.CodeClass
System.out.println(Thread.currentThread().getContextClassLoader().loadClass("xyz.kail.blog.CodeClass")); 

// xyz.kail.blog.CodeClass@372f7a8d
System.out.println(myClassLoader.loadClass("xyz.kail.blog.CodeClass").newInstance());

//classloader.MyClassLoader@24d46ca6
new Thread(() -> System.out.println(Thread.currentThread().getContextClassLoader())).start(); //
```

# PS

深入了解的话可以通过查看 ClassLoader 的继承结构（IDEA 是 Ctrl+H），查看其它开源项目的ClassLoader 实现，这里不再深入了解。
例如如何打破双亲委派模型、热加载、代码保护、Tomcat项目隔离、热更新等。



# 拓展阅读

> [类 ClassLoader](http://tool.oschina.net/uploads/apidocs/jdk-zh/java/lang/ClassLoader.html)
>
> [Java虚拟机动态类加载的形式化模型](http://www.docin.com/p-1265062986.html)  
>
> [Java 类加载器（ClassLoader）的实际使用场景有哪些？](https://www.zhihu.com/question/46719811)
>
> [双亲委派模型](http://blog.csdn.net/p10010/article/details/50448491)
>
> [类加载器CLASSLOADER的工作机制](https://zhuanlan.zhihu.com/p/20524252)
