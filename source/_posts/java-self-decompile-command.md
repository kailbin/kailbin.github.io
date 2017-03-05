---------------
title: Java 内建反编译工具 — javap
date: 2017-03-05
desc: Java 内建反编译工具 — javap
tags: [Java 内建命令,Java,javap] 
---------------

`javap` 是Java内建的一个反编译(反汇编)工具，可以拆解一个或者多个`.class`文件(disassembles one or more class files)。

<!--more-->

# 相关参数

```
-v  -verbose             输出附加信息
-c                       对代码进行反汇编
-constants               显示最终常量


-package                 显示程序包/受保护的/公共类和成员 (默认)
-l                       输出行号和本地变量表
-p  -private             显示所有类和成员


-help  --help  -?        输出此用法消息
-version                 版本信息
-public 或 无参数        仅显示公共类和成员
-protected               显示受保护的/公共类和成员
-s                       输出内部类型签名
-sysinfo                 显示正在处理的类的系统信息 (路径, 大小, 日期, MD5 散列)
-classpath <path>        指定查找用户类文件的位置
-cp <path>               指定查找用户类文件的位置
-bootclasspath <path>    覆盖引导类文件的位置
```

# 示例与解释

## 测试类源码
``` 
public class JavapMain {

    public static final String HELLO = "Hello";

    public static void main(String[] args) {
        String hello = "Hello";

        String helloWorld1 = "Hello World";
        String helloWorld2 = HELLO + " World";

        String helloWorld3 = hello + " World";
    }
}
```



## javap -v JavapMain.class 效果如下

假设 `JavapMain.class` 文件在 `/test/src` 目录下。

```
$ javap  -v JavapMain.class

Classfile /test/src/JavapMain.class
  Last modified 2017-3-5; size 752 bytes
  MD5 checksum ecde8d7a94c4b3fb885fb831360ef555
  Compiled from "JavapMain.java"
public class JavapMain
  minor version: 0
  major version: 51
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
   #1 = Methodref          #10.#31        // java/lang/Object."<init>":()V
   #2 = String             #32            // Hello
   #3 = String             #33            // Hello World
   #4 = Class              #34            // JavapMain
   #5 = Class              #35            // java/lang/StringBuilder
   #6 = Methodref          #5.#31         // java/lang/StringBuilder."<init>":()V
   #7 = Methodref          #5.#36         // java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
   #8 = String             #37            //  World
   #9 = Methodref          #5.#38         // java/lang/StringBuilder.toString:()Ljava/lang/String;
  #10 = Class              #39            // java/lang/Object
  #11 = Utf8               HELLO
  #12 = Utf8               Ljava/lang/String;
  #13 = Utf8               ConstantValue
  #14 = Utf8               <init>
  #15 = Utf8               ()V
  #16 = Utf8               Code
  #17 = Utf8               LineNumberTable
  #18 = Utf8               LocalVariableTable
  #19 = Utf8               this
  #20 = Utf8               LJavapMain;
  #21 = Utf8               main
  #22 = Utf8               ([Ljava/lang/String;)V
  #23 = Utf8               args
  #24 = Utf8               [Ljava/lang/String;
  #25 = Utf8               hello
  #26 = Utf8               helloWorld1
  #27 = Utf8               helloWorld2
  #28 = Utf8               helloWorld3
  #29 = Utf8               SourceFile
  #30 = Utf8               JavapMain.java
  #31 = NameAndType        #14:#15        // "<init>":()V
  #32 = Utf8               Hello
  #33 = Utf8               Hello World
  #34 = Utf8               JavapMain
  #35 = Utf8               java/lang/StringBuilder
  #36 = NameAndType        #40:#41        // append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
  #37 = Utf8                World
  #38 = NameAndType        #42:#43        // toString:()Ljava/lang/String;
  #39 = Utf8               java/lang/Object
  #40 = Utf8               append
  #41 = Utf8               (Ljava/lang/String;)Ljava/lang/StringBuilder;
  #42 = Utf8               toString
  #43 = Utf8               ()Ljava/lang/String;
{
  public static final java.lang.String HELLO;
    descriptor: Ljava/lang/String;
    flags: ACC_PUBLIC, ACC_STATIC, ACC_FINAL
    ConstantValue: String Hello

  public JavapMain();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 1: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   LJavapMain;

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=5, args_size=1
         0: ldc           #2                  // String Hello
         2: astore_1
         3: ldc           #3                  // String Hello World
         5: astore_2
         6: ldc           #3                  // String Hello World
         8: astore_3
         9: new           #5                  // class java/lang/StringBuilder
        12: dup
        13: invokespecial #6                  // Method java/lang/StringBuilder."<init>":()V
        16: aload_1
        17: invokevirtual #7                  // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
        20: ldc           #8                  // String  World
        22: invokevirtual #7                  // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
        25: invokevirtual #9                  // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
        28: astore        4
        30: return
      LineNumberTable:
        line 6: 0
        line 8: 3
        line 9: 6
        line 11: 9
        line 12: 30
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      31     0  args   [Ljava/lang/String;
            3      28     1 hello   Ljava/lang/String;
            6      25     2 helloWorld1   Ljava/lang/String;
            9      22     3 helloWorld2   Ljava/lang/String;
           30       1     4 helloWorld3   Ljava/lang/String;
}
SourceFile: "JavapMain.java"
```

## -v 参数的简单解释

### 3-6行
显示了 最后修改时间、文件大小、MD5校验码、文件名 信息。

### 7-10行
显示了 class 文件的版本号、类的访问表示信息。
这里显示主版本号是 51，根据一个简单的公式推断**[51 - 44 = `7`（主版本号-44）]**，说明该类可以被 7 以上的 JDK 执行。 

`ACC_PUBLIC` 说明该类是 pubilc 访问级别的。

> ACC_SUPER 标志用于确定该 Class 文件里面的 invokespecial 指令使用的是哪一种执行语义。
> 目前 Java 虚拟机的编译器都应当设置这个标志。 
> ACC_SUPER 标记 是为了向后兼容旧编译器编译的 Class 文件而存在的，在 JDK1.0.2 版本以前的编译器产生的 Class 文件中， access_flag 里面没有 ACC_SUPER 标志。
> 同时，JDK1.0.2 前的 Java 虚拟机遇到 ACC_SUPER 标记会自动忽略它。
>                                        —— 《Java 虚拟机规范(1.7)中文版》

除此之外还有 `ACC_FINAL` 不允许有子类、`ACC_INTERFACE` 标识定义的是接口而不是类、 `ACC_ABSTRACT` 不能被实例化、 `ACC_SYNTHETIC` 标识并非 Java 源码生成的代码

### 11-54行 
为常量池信息，每一个常量都有一个以 # 开头的表示，供后面引用。

### 56-59行
说明该类中有一个 名为 `HELLO` 的 `public static final String` 常量，常量值是 `Hello`。

### 61-73行
显示了该类的 **默认无参数构造该方法**。
可以看出 默认构造方法，默认会调用父类 `Object` 的默认默认构造方法。

### 75-109行
main 方法内的信息

#### 78-95行
为实际执行的 JVM 指令，指令后面的 `#n` 为引用常量池中的常量。

**80-85** 基本上就是把字符串从常量池推送至栈顶，然后将其存入局部变量。 **需要注意的是84行，源码中是一个常量和一个字符串相加，编译后变成了一个完整的字符串。**

**86-94** 也比较特殊，源码是 **一个局部变量和一个字符串相加**，实际上变成了 new 一个 StringBuilder，然后进行 append 操作，最后再 toString。 

#### 76-108行
为 行号表（LineNumberTable） 和 本地变量表 （LocalVariableTable） 的映射关系。
LocalVariableTable 中显示出有哪些局部变量。

# 本文涉及到的 Java 虚拟机指令

| 指令 | 解释 |
|--:|:--|
| aload_0  | 将第一个引用类型局部变量推送至栈顶 |
| aload_1  | 将第二个引用类型局部变量推送至栈顶 |
| astore   | 将栈顶引用型数值存入指定局部变量 |
| astore_1 | 将栈顶引用型数值存入第二个局部变量 |
| dup      | 复制栈顶数值并将复制值压入栈顶 |
| invokespecial | 调用超类构造方法，实例初始化方法，私有方法 |
| invokevirtual | 调用实例方法 |
| ldc           | 将 int， float 或 String 型常量值从常量池中推送至栈顶 |
| new           | 创建一个对象，并将其引用值压入栈顶 |
| return        | 从当前方法返回 void |


详情查看 拓展阅读中 《Java 虚拟机规范》 的 Java 虚拟机指令集相关章节。

# PS
javap 的 -v 参数是输出内容最详细的参数了，基本上包含了其它参数输出的内容，这里就不再举例其它参数的使用方法了。

# 拓展阅读

> [javap](http://docs.oracle.com/javase/8/docs/technotes/tools/windows/javap.html)
>
> Java 虚拟机规范 英文 （[The Java® Virtual Machine Specification](http://docs.oracle.com/javase/specs/jvms/se8/html/index.html)） 
> 
> [Java虚拟机规范(Java SE 7版)](https://book.douban.com/subject/25792515/)
>
> Java 反编译工具 [jd-gui](https://github.com/java-decompiler/jd-gui/releases)
