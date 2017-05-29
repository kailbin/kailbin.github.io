---
title: javassist 入门
date: 2017-05-29
desc: javassist 入门, javassist 笔记

tags: [javassist]
categories: Java
---

Javassist 是一个操作 Java 字节码的类库，通过它可以直接操作 Java 的 `.class` 字节码文件。


<!--more-->

# 使用 Javassist 创建一个 class 文件

`Javassist.CtClass` 是类文件的抽象。 `CtClass`（compile-time class）负责处理一个类文件 。下面是个简单的例子：

``` java
ClassPool pool = ClassPool.getDefault();

// 1. 创建一个空类
CtClass cc = pool.makeClass("xyz.kail.blog.CodeClass");

// 2. 新增一个字段 private String name = "init";
CtField param = new CtField(pool.get("java.lang.String"), "name", cc); // 字段名为name
param.setModifiers(Modifier.PRIVATE); // 访问级别是 private
cc.addField(param, CtField.Initializer.constant("init")); // 初始值是 "init"

// 3. 生成 getter、setter 方法
cc.addMethod(CtNewMethod.setter("setName", param));
cc.addMethod(CtNewMethod.getter("getName", param));

// 4. 添加无参的构造函数
CtConstructor cons = new CtConstructor(new CtClass[]{}, cc);
cons.setBody("{name = \"Kail\";}");
cc.addConstructor(cons);

// 5. 添加有参的构造函数
// http://jboss-javassist.github.io/javassist/tutorial/tutorial2.html#before
cons = new CtConstructor(new CtClass[]{pool.get("java.lang.String")}, cc);
cons.setBody("{$0.name = $1;}"); // $0=this / $1,$2,$3... 代表方法参数
cc.addConstructor(cons);

// 6. 创建一个名为execute方法，无参数，无返回值，输出name值
CtMethod ctMethod = new CtMethod(CtClass.voidType, "execute", new CtClass[]{}, cc);
ctMethod.setModifiers(Modifier.PUBLIC);
ctMethod.setBody("{System.out.println(name);}");
cc.addMethod(ctMethod);

cc.writeFile("/Users/kail/_test");
```

运行之后，找到生成的类文件 `/Users/kail/_test/xyz/kail/blog/CodeClass.class`，反编译后的效果如下：
``` java
package xyz.kail.blog;

public class CodeClass {

    private String name = "init";

    public void setName(String var1) {
        this.name = var1;
    }

    public String getName() {
        return this.name;
    }

    public CodeClass() {
        this.name = "Kail";
    }

    public CodeClass(String var1) {
        this.name = var1;
    }

    public void execute() {
        System.out.println(this.name);
    }
}

```

# 调用生成的类

上面生成了一个类，如何调用这个类呢？ 例子如下：

## 实例化调用

``` java
 ... 省略生成类的部分

// cc.writeFile("/Users/kail/_test");
Object codeClass = cc.toClass().newInstance(); // 这里不写入文件，直接实例化

// 设置值
Method setName = codeClass.getClass().getMethod("setName", String.class);
setName.invoke(codeClass, "Mr.Kail");

// 输出值
Method execute = codeClass.getClass().getMethod("execute");
execute.invoke(codeClass);
```

## 读取文件调用

``` java
ClassPool pool2 = ClassPool.getDefault();
pool2.appendClassPath("/Users/kail/_test"); // 设置类路径
CtClass ctClass = pool2.get("xyz.kail.blog.CodeClass");
Object codeClass = ctClass.toClass().newInstance();

... 省略反射的调用的部分（调用方式同上）
```

## 通过接口方式调用

以上生成类之后，通过反射的方式调用，个人感觉实际场景比较少，因为反射的性能不是很好。实际上可以定义一个接口，通过接口调用，如下：

先定义给一个接口

``` java
package xyz.kail.blog;

public interface CodeClassI {

    void setName(String name);

    String getName();

    void execute();
}
```

``` java 
ClassPool pool = ClassPool.getDefault();
pool.appendClassPath("/Users/kail/_test");

CtClass codeClassI = pool.get("xyz.kail.blog.CodeClassI"); // 获取接口
CtClass ctClass = pool.get("xyz.kail.blog.CodeClass"); // 获取上面生成的类
ctClass.setInterfaces(new CtClass[]{codeClassI}); // 使代码生成的类，实现 xyz.kail.blog.CodeClassI 接口

// 以下通过接口直接调用
CodeClassI codeClass = (CodeClassI)ctClass.toClass().newInstance(); // 实例化代码生成的类，这个类是 CodeClassI 的实现，可以直接强转成 CodeClassI
System.out.println(codeClass.getName());
codeClass.setName("Mr.Kail");
codeClass.execute();
```


# 使用 Javassist 修改现有的类

通过代码去凭空生成一个类估计很少用到，因为这哪有直接写一个类方便。
主要的场景是修改一个现有的类，给类增加功能，如AOP等。

## 在方法前后插入代码

``` java
CtMethod executeMethod = ctClass.getDeclaredMethod("execute");
executeMethod.insertBefore("org.slf4j.LoggerFactory.getLogger(xyz.kail.blog.CodeClass.class).info(\"--开始打印\");");
executeMethod.insertAfter("org.slf4j.LoggerFactory.getLogger(xyz.kail.blog.CodeClass.class).info(\"--打印完成\");");
```

## 清空方法体使方法无效

``` java
CtMethod executeMethod = ctClass.getDeclaredMethod("execute");
executeMethod.setBody("{}");
```

其它更复杂的修改请参考官方文档 [Introspection and customization](http://jboss-javassist.github.io/javassist/tutorial/tutorial2.html#intro)。



# 拓展阅读

>[Javassist 官方文档](http://jboss-javassist.github.io/javassist/)  
>
> [javassist 学习笔记](http://zhxing.iteye.com/blog/1703305)
