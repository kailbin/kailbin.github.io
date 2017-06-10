---
title: Java 泛型信息真的全都擦除了吗
date: 2017-06-09
desc: Java 泛型信息真的全都擦除了吗

tags: [Java]
categories: Java
---

JDK5 引入了泛型，为了保持跟 JDK5 之前兼容性，Java采用 **泛型擦除** 的机制来引入泛型。
所以说 Java 的泛型实际上是伪泛型，其仅仅是给编译器javac使用的，确保数据的安全性和免去强制类型转换的麻烦。
在运行期，Java的泛型将被擦除，使用强转的形式来进行（类似于`Integer value = (Integer) map.get("key")`）。

<!--more-->

# 泛型擦除的例子1

编译前
``` java
    Map<String, Integer> map = new HashMap<>();
    map.put("one", 1);
    System.out.println(map.get("one") + 1);
```

编译后
``` java
    HashMap localHashMap = new HashMap();
    localHashMap.put("one", Integer.valueOf(1));
    System.out.println(((Integer)localHashMap.get("one")).intValue() + 1);
```

# 泛型擦除的例子2
编译前
``` java
    Class<? extends ArrayList> stringList = new ArrayList<String>().getClass();
    Class<? extends ArrayList> integerList = new ArrayList<Integer>().getClass();
    System.out.println(stringList == integerList);
```

编译后
``` java
    Class localClass1 = new ArrayList().getClass();
    Class localClass2 = new ArrayList().getClass();
    System.out.println(localClass1 == localClass2);
```
这里输出 `true`，可以看出，新建的两个不同的 `ArrayList<>`，编译之后都是`ArrayList`，没有了泛型信息。

以上两个例子均使用 `javac Main.java` 编译，使用 `jd-gui-1.4.0.jar` 反编译，测试中发现，不同的版本的编译和反编译工具出来的结果有些许不同。

# JDK5 之后类文件(.class 文件)的变动

> The Java SE 5.0 platform in 2004 brought momentous changes to the Java programming language but had a relatively muted effect on the design of the Java Virtual Machine. Additions were made to the class file format to support new Java programming language features such as generics and variable arity methods.

> 在 2004 年发布的 Java SE 5.0 版为 Java 语言带来了翻天覆地的变化， 但是对 Java 虚拟机设计上的影响力则相对较小。 在这个版本中， 我们扩充了 Class 文件格式以便支持新的 Java语言特性，譬如<u>泛型</u>和<u>变长参数方法</u>等。

> From [《Java Virtual Machine Specification》 - Preface to the Java SE 7 Edition ](http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-0-preface7.html)

添加了泛型支持后，.class 文件格式也发生了变动。其通过 Signature(签名) 的形式来记录定义的 泛型类、泛型方法等。
> 签名（Signature） 是用于描述字段、方法和类型定义中的泛型信息的字符串;
> 泛型类型、方法描述和参数化类型描述等都属于签名;
> Java 编译器需要这类信息来实现（或辅助实现） 反射（reflection） 和跟踪调试功能;

详见：[《Java Virtual Machine Specification》 - 4.3.4. Signatures](http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.4)


# 如何获得声明的泛型类型

``` java
/**
 * References a generic type.
 * <p>
 * http://gafter.blogspot.nl/2006/12/super-type-tokens.html
 *
 * @author crazybob@google.com (Bob Lee)
 */
public abstract class MyTypeReference<T> {

    private final Type type;

    protected MyTypeReference() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0]; // !!!!! 关键部分
    }

    @SuppressWarnings("unchecked")
    public T newInstance() throws IllegalAccessException, InstantiationException {
        return (T) newInstance(type);
    }

    /**
     * 实例化获得的类型
     */
    public static Object newInstance(Type type) throws IllegalAccessException, InstantiationException {
        Class<?> klass;
        if (type instanceof Class<?>) {
            klass = (Class<?>) type;
        } else {
            klass = (Class<?>) ((ParameterizedType) type).getRawType();
        }
        return klass.newInstance();
    }

    public Type getType() {
        return this.type;
    }

    /**
     * 打印出所有的泛型类型
     */
    public void printAllType() {
        System.out.println(type);
        printAllType(type, "\t");
    }

     private void printAllType(Type tp, String tab) {
        // 是否是参数化类型 （类似于 Map<String, Integer> 中的 `String` 和 `Integer`）
        if (tp instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) tp;
            System.out.println(tab + parameterizedType.getRawType());

            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            for (Type actualTypeArgument : actualTypeArguments) {
                printAllType(actualTypeArgument, tab + "\t");
            }
        } // 是否是 类类型
        else if (tp instanceof Class) {
            System.out.println(tab + tp);
        }  // 是否是通配符类型 （类似以与 List<? extends Map> 中的 `? extends Map`）
        else if (tp instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) tp;
            System.out.println(tab + wildcardType);

            // `? super Map`
            Type[] lowerBounds = wildcardType.getLowerBounds();
            if (lowerBounds.length > 0) {
                for (Type lowerBound : lowerBounds) {
                    printAllType(lowerBound, tab + "\t");
                }
            }
            // `? extends Map`
            Type[] upperBounds = wildcardType.getUpperBounds();
            if (upperBounds.length > 0) {
                for (Type upperBound : upperBounds) {
                    printAllType(upperBound, tab + "\t");
                }
            }
        }
    }
}
```
该类是抽象类，获得泛型信息的时候new其子类，如下：

## 使用示例1 ParameterizedType
``` java
new MyTypeReference<ArrayList<Map<LinkedHashMap<String, int[]>, String>>>() {
}.printAllType();
```

打印 出所有的泛型信息
``` 
java.util.ArrayList<java.util.Map<java.util.LinkedHashMap<java.lang.String, int[]>, java.lang.String>>
    class java.util.ArrayList
        interface java.util.Map
            class java.util.LinkedHashMap
                class java.lang.String
                class [I
            class java.lang.String
```

## 使用示例2 WildcardType
``` java
new MyTypeReference<List<? extends Map>>() {
}.printAllType();
```

打印 出所有的泛型信息
``` 
java.util.List<? extends java.util.Map>
	interface java.util.List
		? extends java.util.Map
			interface java.util.Map
```

需要注意的是，这里使用的都是 匿名子类（`new MyTypeReference<...>() { }`）。

# 常见应用场景
Spring 中的 `ParameterizedTypeReference<T>`、fastjson 中的`TypeReference<T>`等，皆是通过该方式获得泛型信息，并反序列化json的。

# 总结

## RednaxelaFX 说
> Java泛型有这么一种规律：
> 位于声明一侧的，源码里写了什么到运行时就能看到什么;
> 位于使用一侧的，源码里写什么到运行时都没了。

什么意思呢?
> “声明一侧”包括泛型类型(泛型类与泛型接口)声明、带有泛型参数的方法和域的声明。
> 注意局部变量的声明不算在内，那个属于“使用”一侧


# 拓展阅读

> [4.3.4. Signatures](http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.4)
>
> [为什么能获取到父类的泛型？](https://segmentfault.com/q/1010000005680647)
> [详解Java泛型type体系整理](http://developer.51cto.com/art/201103/250028.htm)
> [Java类型中ParameterizedType，GenericArrayType，TypeVariabl，WildcardType详解](http://www.webkfa.com/one7/w595.html)

