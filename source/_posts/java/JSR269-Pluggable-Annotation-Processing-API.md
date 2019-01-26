---
title: Pluggable Annotation Processing API
categories:
  - Java
date: 2018-12-01
id: jsr269
---



JSR 269（Pluggable Annotation Processing API）提供一套标准API来使用 **Annotation Processor 在编译期间而不是运行期间处理Annotation** ，**相当于编译器的一个插件**，是 JDK 6 的一个新特性。

<!-- more -->

本文只是一个 Hello Word 的示例，效果只是在使用 javac 编译的时候在控制台输出一句话。如果想实现类似于 lombok 在编译期通过注解修改原有类文件的效果，可以查看最后参考资料里面的链接。


# 元注解回顾

在了解 JSR-269 之前，先了解一下 注解的注解 —— 元注解（用来描述注解的注解）

## @Target
定义注解可使用的地方

```java
public enum ElementType {
    /** 适用于 class, interface, enum  */
    TYPE,

    /** 适用于 字段 */
    FIELD,

    /** 适用于 方法 */
    METHOD,

    /** 适用于 方法参数 */
    PARAMETER,

    /** 适用于 构造函数 */
    CONSTRUCTOR,

    /** 适用于 局部变量 */
    LOCAL_VARIABLE,

    /** 适用于 注解类型 */
    ANNOTATION_TYPE,

    /** 适用于 包 */
    PACKAGE,

    /**
     * 适用于 泛型参数 如： public class Test<@Demo T> { }
     *
     * @since 1.8
     */
    TYPE_PARAMETER,

    /**
     * 适用于 类型 如：(@Demo String) new Object()
     *
     * @since 1.8
     */
    TYPE_USE
}

```

> [【JDK8】Annotation 功能增強](https://openhome.cc/Gossip/CodeData/JDK8/Annotation.html)

## @Retention

```java
public enum RetentionPolicy {
    /**
     * 注解不会被编译到 .class 文件中
     */
    SOURCE,

    /**
     * [默认行为] 注解会被编译到 .class文件中，但不会被JVM加载
     */
    CLASS,

    /**
     * JVM会把注解加载到内存里，运行期间可见，所以可以通过反射读取注解的信息
     *
     * @see java.lang.reflect.AnnotatedElement
     */
    RUNTIME
}
```

## @Documented
声明该注解应包含在 javadoc 中

## @Inherited
使用此注解声明出来的自定义注解，在使用此自定义注解时，**如果注解在类上面时，子类会自动继承此注解，否则的话，子类不会继承此注解**

> [关于java 注解中元注解Inherited的使用详解](https://blog.csdn.net/snow_crazy/article/details/39381695)

# 注解处理器（Annotation Processor）

JSR 269 的核心是 Annotation Processor 即注解处理器，与运行时注解`RetentionPolicy.RUNTIME`不同，**注解处理器只会处理编译期注解**，也就是`RetentionPolicy.SOURCE`的注解类型，处理的阶段位于Java代码编译期间。

> 如果 Annotation Processor 处理 Annotation 时产生了新的Java代码，编译器会再调用一次Annotation Processor，如果第二次处理还有新代码产生，就会接着调用Annotation Processor，直到没有新代码产生为止。

## 使用步骤

- 自定义一个注解，注解的元注解需要指定`@Retention(RetentionPolicy.SOURCE)`
- 创建 注解处理器类 继承`javax.annotation.processing.AbstractProcessor`，并覆写`process`方法
  - 在创建的注解处理器类上使用`javax.annotation.processing.SupportedAnnotationTypes`注解指定 自定义注解的全名
  - 在创建的注解处理器类上使用`javax.annotation.processing.SupportedSourceVersion`注解指定编译版本
  - 【可选】在创建的注解处理器类上使用`javax.annotation.processing.SupportedOptions`注解指定编译参数

## 示例

### S1. 编写 自定义注解

```java
package xyz.kail.demo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 用于修饰 类、接口 ...
@Target({ElementType.TYPE})
// 在编译器期可以见，但是不会被编译到 .class 文件里面
@Retention(RetentionPolicy.SOURCE)
public @interface PrintHello {
}
```

### S2. 创建 注解处理器类

```java
package xyz.kail.demo;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes({"xyz.kail.demo.PrintHello"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class HelloAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        System.out.println("Hello World");

        annotations.forEach(typeElement -> {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(typeElement);
            elements.forEach(element -> {
                System.out.print(element.getEnclosingElement());
                System.out.print(".");
                System.out.println(element.getSimpleName());
            });
        });

        return true;
    }
}

```

### S3. 启动类

```java
package xyz.kail.demo;

@PrintHello
public class Main {
    public static void main(String[] args) {
    }
}
```

### S4. 编译 注解处理器类

在使用 javac 编译的时候，注解处理器类 必须事先已经被编译过了，所需要先编译 HelloAnnotationProcessor：

```bash
javac xyz/kail/demo/HelloAnnotationProcessor.java 
```

### S5. 编译 被自定义注解修饰的类 

请注意 `-processor` 参数，用来指定注解处理器

```bash
javac -processor xyz.kail.demo.HelloAnnotationProcessor  xyz/kail/demo/Main.java
```

编译时输出如下：

```
Hello World
xyz.kail.demo.Main
Hello World
```

# 启用注解处理器的几种方式

在编译时启用注解处理器 除了 `-processor` 参数外，还有以下两种常见方式

## SPI 服务提供者发现机制

就是把注解处理器事先打成 jar 包，并在 jar 包内的 `META-INF/services/` 文件夹内包含 文件 `javax.annotation.processing.Processor`，文件内容是 注解处理器的全路径名（`xyz.kail.demo.HelloAnnotationProcessor`）

```
xxx.jar
|-META-INF
  |-services
    |-javax.annotation.processing.Processor(文件内容如下)
       |-xyz.kail.demo.HelloAnnotationProcessor
```



## Maven

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>${version}</version>
    <configuration>
        <source>1.8</source>
        <target>1.8</target>
        <encoding>UTF-8</encoding>
        <annotationProcessors>
            <annotationProcessor>
                xyz.kail.demo.HelloAnnotationProcessor
            </annotationProcessor>
        </annotationProcessors>
    </configuration>
</plugin>
```

# 参考资料

- [Java-JSR-269-插入式注解处理器](https://liuyehcf.github.io/2018/02/02/Java-JSR-269-%E6%8F%92%E5%85%A5%E5%BC%8F%E6%B3%A8%E8%A7%A3%E5%A4%84%E7%90%86%E5%99%A8/)
- [Java奇技淫巧-插件化注解处理API(Pluggable Annotation Processing API)](https://www.cnblogs.com/throwable/p/9139908.html)
- [Java Annotation Processing and Creating a Builder](https://www.baeldung.com/java-annotation-processing-builder)
- [[译]使用注解处理器生成代码-1 注解类型](https://www.jianshu.com/p/ed2a755fc053)
- [[译]使用注解处理器生成代码-2 注解处理器](https://www.jianshu.com/p/d294bf008bec)
- [[译]使用注解处理器生成代码-3 生成源代码](https://www.jianshu.com/p/2a2273ca1b69)

