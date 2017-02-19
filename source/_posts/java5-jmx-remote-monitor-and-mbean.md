---
title: JMX 远程监控 与 MBean
date: 2017-2-19
desc: JMX,Java远程监控,MBean

tags: [Java,JMX,jconsole,jvisualVM]

---

JMX(Java Management Extensions) 是 Java管理扩展，MBean(Managed Beans) 是 被管理的Beans。

> 一个MBean是一个被管理的Java对象，有点类似于JavaBean，一个设备、一个应用或者任何资源都可以被表示为MBean，MBean会对外暴露一个接口，这个接口可以读取或者写入一些对象中的属性。


<!--more-->

# MBean


## 定义MBean接口

``` java
package xyz.kail.blog.jmx;

/**
 * 定义 MBean 接口，与一般的接口类似，**但是必须以MBean结尾**
 * <p>
 * Created by kail on 2017/2/19.
 */
public interface HelloWorldMBean {

    /**
     * 定义 MBean 的名字
     */
    String TYPE_HELLO_WORLD = "xyz.kail.blog.jmx:type=HelloWorld";

    /**
     * 一般方法会以"操作"的形式在 JConsole 或者 JvisualVM 显示
     */
    void hello();

    void say(String name);


    /**
     * get/set 方法会以"属性"的形式在 JConsole 或者 JvisualVM 显示
     */
    String getName();

    void setName(String name);

}  
```

## 实现MBean接口

``` java
package xyz.kail.blog.jmx;

/**
 * Created by kail on 2017/2/19.
 */
public class HelloWorld implements HelloWorldMBean {

    private String name = "Kail";

    /**
     * 打印属性名
     */
    @Override
    public void hello() {
        System.out.println("Hello World ：" + this.name);
    }

    @Override
    public void say(String name) {
        System.out.println("Hello " + name + " !");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

}  
```


## 注册 MBean

``` java
package xyz.kail.blog.jmx;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * Created by kail on 2017/2/19.
 */
public class StartMain {

    public static void main(String[] args) throws Exception {

        // 获取 MBeanServer
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        // 注册 MBean
        mbs.registerMBean(new HelloWorld(), new ObjectName(HelloWorldMBean.TYPE_HELLO_WORLD));

        // 结束程序的时候在控制台随便输入点什么
        System.in.read();
    }
}
```

# jvisualVM 中的 MBean

`jvisualVM` 默认没有 MBean 的 tab，不过 `jvisualVM` 可以安装插件，选择 菜单 > 工具 > 插件 > 可用插件 > VisualVM-MBeans > 安装即可。


## 设置属性

如下图所示，可以查看 MBean 中定义的属性，点击 Value 值即可编辑。

![jvisualVM 设置属性](/images/java5-jmx-remote-monitor-and-mbean/1.png)

## 运行操作

如下图所示，MBean 中定义的方法名是一个按钮，点击即可运行。

运行之后在 StartMain 控制台查看输出。效果跟直接调用本地方式是一样的。

![jvisualVM 运行操作](/images/java5-jmx-remote-monitor-and-mbean/2.png)



# 开启远程监控

开启之后，可以用 `jconsole` 或 `jvisualVM` 等，在本地 通过JMX的方式 对远程应用进行监控和分析，以下是无认证方式：

``` 
-Dcom.sun.management.jmxremote=true             
-Dcom.sun.management.jmxremote.port=<port>              # 监控端口
-Dcom.sun.management.jmxremote.ssl=false                
-Dcom.sun.management.jmxremote.authenticate=false       
```

添加到远程应用的 JVM 启动参数上即可。

# PS

JXM 技术的典型应用包括:

- 访问和改变应用程序配置
- 统计应用程序的行为信息，以便优化程序
- 状态变化和通知

JMX API 也提供远程访问的功能，所有以上场景的需要话，可以远程与应用程序进行交互。

> Typical uses of the JMX technology include:
> 
> - Consulting and changing application configuration
> - Accumulating statistics about application behavior and making them available
> - Notifying of state changes and erroneous conditions.
>
> The JMX API includes remote access, so a remote management program can interact with a running application for these purposes.

本文只是简单的介绍一下，以便有一个概念性的认识，想要深入了解可以参考 拓展阅读。

# 拓展阅读

> [Java Management Extensions (JMX)](http://docs.oracle.com/javase/8/docs/technotes/guides/jmx/index.html)
>
> [软件包 javax.management](http://tool.oschina.net/uploads/apidocs/jdk-zh/javax/management/package-summary.html) 提供 Java Management Extensions 的核心类
>
> [JMX学习笔记(一)-MBean](http://tuhaitao.iteye.com/blog/786391)
