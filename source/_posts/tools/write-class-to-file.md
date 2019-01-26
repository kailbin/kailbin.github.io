---
title: 把Java类写入到.class文件
categories:
  - Tools
tags:
  - Java
date: 2018-11-03
id: java/write-class-to-file
---

这里说的 把Java类写入到.class文件 **不是** Java对象序列化  `ObjectOutputStream` ，Java对象序列化写入的是类中的数据，并不是类本身。

这里说的是如果在运行过程中把类本身字节码写入到文件中，便于反编译查看类的结构信息。

如果类本身就是从文件中加载，直接读取类文件即可，或者根本就不用读取在写入新的文件，因为类文件本身就存在。但是**如果一个类是运行中生成的，如果把这种动态生成的类写入到磁盘呢？**

<!-- more -->

# 1. Java Agent

## Agent

```java
package xyz.kail.demo.agent;

import xyz.kail.demo.agent.transformer.ClassToFileTransformer;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Agent {

    /**
     * 在Java SE5时代，Instrument只提供了premain 一种方式，即在main方法启动前启动一个代理程序
     */
    public static void premain(String args, Instrumentation instrumentation) {
        Map<String, String> params = parseArgs(args);
        if (params.isEmpty()) {
            return;
        }

        /*
         * 启用Transformer
         */
        if (null != params.get("ClassToFileTransformer")) {
            instrumentation.addTransformer(new ClassToFileTransformer(params), true);
        }
    }

    /**
     * key1=value1;key2=value2  --> {key1:value1, key2:value2}
     */
    static Map<String, String> parseArgs(String args) {
        if (null == args) {
            return Collections.emptyMap();
        }

        Map<String, String> argsMap = new HashMap<>();
        String[] kvs = args.split(";");
        for (String kv : kvs) {
            String[] kvArr = kv.split("=");
            argsMap.put(kvArr[0], kvArr[1]);
        }
        return argsMap;
    }
}
```

## ClassFileTransformer

```java
package xyz.kail.demo.agent.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 把加载的类，写入到文件
 */
public class ClassToFileTransformer implements ClassFileTransformer {

    Map<String, String> args;

    public ClassToFileTransformer(Map<String, String> args) {
        this.args = args;
    }

    /**
     * @param className       类名
     * @param classfileBuffer 类的字节码
     */
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        
        if (null != args.get("debug")) {
            System.out.println(className);
        }

        String regex = args.get("regex");
        // 如果传入正则，但是不匹配，不进行操作
        if (!(null != regex && Pattern.matches(regex, className))) {
            return classfileBuffer;
        }


        try {
            Path path = Paths.get("classes/" + className + ".class");
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, classfileBuffer, StandardOpenOption.CREATE);
        } catch (Throwable ignored) {
            if (null != args.get("debug")) {
                ignored.printStackTrace();
            }
        }

        return classfileBuffer;
    }
}
```

## Maven 打包 plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>3.1.0</version>
    <configuration>
        <archive>
            <!--自动添加META-INF/MANIFEST.MF -->
            <manifest>
                <addClasspath>true</addClasspath>
            </manifest>
            <manifestEntries>
                <Main-Class>xyz.kail.demo.agent.AgentTest</Main-Class>
                <!-- Premain-Class: xyz.kail.demo.agent.Agent -->
                <Premain-Class>xyz.kail.demo.agent.Agent</Premain-Class>
            </manifestEntries>
        </archive>
    </configuration>
</plugin>
```

## 启动时加入 java agent 参数

```bash
java -jar \ 
-javaagent:agent.jar=ClassToFileTransformer=true;debug=true;regex=xyz.+ \
xxx.jar
```

> [如何指导编写一个javaagent](https://blogs.oracle.com/ouchina/javaagent)

# 2. 运行时添加 Java Agent

上面 `premain` 类型的 Java Agent 只能在程序运行前，在启动命令中指定 javaagent 参数 才能生效。

Java SE 6 新特性改变了这种情况，通过 Java Tool API 中的 attach 方式，我们可以很方便地在运行过程中动态地设置加载代理类。

## Agent

在 Agent 类中新增 `agentmain` 方法，参数和 `premain` 中的是一样的

```java
public class Agent {

    /**
     * 运行时 Agent
     */
    public static void agentmain(String args, Instrumentation instrumentation) throws UnmodifiableClassException, IllegalAccessException, InstantiationException {
        Map<String, String> params = parseArgs(args);
        if (params.isEmpty()) {
            return;
        }

        /*
         * 调用 premain 注册 Transformer
         */
        premain(args, instrumentation);


        String regex = params.get("regex");
        if (null == regex) {
            return;
        }

        // 获取所有已加载的 Class
        Class[] allLoadedClasses = instrumentation.getAllLoadedClasses();
        for (Class clazz : allLoadedClasses) {
            // 因为是 attach 到运行中的 JVM 进程，类已经加载的内存了，这里需要重新转换类
            if (Pattern.matches(regex, clazz.getName())) {
                instrumentation.retransformClasses(clazz);
            }
        }
    }
    
    ...
    
}
```

## 依赖 tools 包

```xml
<dependency>
    <groupId>com.sun</groupId>
    <artifactId>tools</artifactId>
    <version>1.8</version>
    <scope>system</scope>
    <systemPath>${java.home}/../lib/tools.jar</systemPath>
</dependency>
```



## Maven 打包 plugin

```xml
<plugin>
    <artifactId>maven-jar-plugin</artifactId>
    <configuration>
        <archive>
            ...
            <manifestEntries>
                ... META-INF/MANIFEST.MF 中新加入以下连个属性
                <Agent-Class>xyz.kail.demo.agent.Agent</Agent-Class>
                <Can-Retransform-Classes>true</Can-Retransform-Classes>
            </manifestEntries>
        </archive>
    </configuration>
</plugin>
```

## Attach

```java
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.util.List;

public class AgentTest {

    public static void main(String[] args) throws Exception {

        List<VirtualMachineDescriptor> vmDescriptors = VirtualMachine.list();
        for (VirtualMachineDescriptor vm : vmDescriptors) {
            System.out.println(vm.id() + " : " + vm.displayName());
        }

        VirtualMachine vm = VirtualMachine.attach("pid");
        try {
            vm.loadAgent("agent-1.0.0.jar","ClassToFileTransformer=true;debug=true;regex=proxy.+");
        } finally {
            vm.detach();
        }
    }
}
```

或 通过 jar 运行上面的 AgentTest.main 方法

```java
java -Xbootclasspath/a:${JAVA_HOME}lib/tools.jar -jar target/agent-1.0.0.jar
```

> [Java SE 6 新特性 Instrumentation 新功能](https://www.ibm.com/developerworks/cn/java/j-lo-jse61/index.html)

# 3. HSDB

HSDB(Hotspot Debugger) 是 `${JAVA_HOME}/lib/sa-jdi.jar` 中提供的一个图形化界面工具，可以查看Java对象的oops、查看类信息、线程栈信息、堆信息、方法字节码 和 JIT编译后的汇编代码 等，可以说非常底层全面。

sa 全称 Serviceability Agent ，原本是Sun公司用来debug Hotspot的工具，后来开放给Hotspot使用者，能够查看Java的内部数据结构等信息，它可以直接观察一个JVM 进程。

**步骤**

- 启动图形化界面：`sudo java -cp ${JAVA_HOME}/lib/sa-jdi.jar sun.jvm.hotspot.HSDB`
- Attach JVM 进程：菜单 -> File -> Attach to HotSpot process
- 查看所有运行中的类：菜单 -> Tools -> Class Browser
- 下载类字节码到文件： 搜索指定的类，点击 “Create .class for all classes” 就会下载指定的类到类路径下



> [借HSDB来探索HotSpot VM的运行时数据](http://rednaxelafx.iteye.com/blog/1847971)
>
> [通过HSDB来了解String值的真身在哪里](http://lovestblog.cn/blog/2014/06/28/hsdb-string/)