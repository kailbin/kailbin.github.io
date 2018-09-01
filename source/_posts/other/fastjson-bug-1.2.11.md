
---
title: 记一次 fastjson 泛型反序列化的 Bug
date: 2018-09-02
tags: [Bug]
categories:
  - Other
id: fastjson-bug-1.2.11
---

最近 使用 fastjson 的时候发现一个问题，偶尔会报 类型转换异常 `java.lang.ClassCastException: com.alibaba.fastjson.JSONObject cannot be cast to XXX`，而报错的地方时使用 的 `TypeReference`，泛型信息是传给 fastjson 的，并不是 XXX.class 也没有对返回值进行强转。

在网上搜了一些 fastjson 类型转换错误的文章，大部分是都是使用问题造成，最后发现了这篇文章[《FastJson 泛型转换踩坑》](https://blog.csdn.net/ykdsg/article/details/50432494)，才基本确定是 fastjon 对泛型类反序列的一个Bug。


<!-- more -->

以下是复现 Bug 的代码，可以看出同样的代码，调用的顺序不一样，结果也会不一样。

# fastjson 版本

``` xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <!-- bug -->
    <!--<version>1.2.0</version>-->
    <!-- bug -->
    <version>1.2.11</version>
    <!-- 修复 -->
    <!--<version>1.2.12</version>-->
</dependency>
```


# 辅助类

## WebResult.java

``` java
public class WebResult<T> {

    private T result;

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}

```

## Person.java

``` java
public class PersonVO {
}
```

# 示例代码

``` java
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

public class GenericTypeTest {


    private static void error1() {
        // 1.2.11 class com.alibaba.fastjson.JSONObject
        // 1.2.12 class com.alibaba.fastjson.JSONObject
        ptl(JSON.parseObject("{'result':{}}", WebResult.class).getResult().getClass());
        // 1.2.11 java.lang.ClassCastException: com.alibaba.fastjson.JSONObject cannot be cast to PersonVO
        // 1.2.12 class PersonVO
        ptl(JSON.parseObject("{'result':{}}", new TypeReference<WebResult<PersonVO>>() {
        }).getResult().getClass());
    }

    private static void success1() {
        // 1.2.11 class PersonVO
        // 1.2.12 class PersonVO
        ptl(JSON.parseObject("{'result':{}}", new TypeReference<WebResult<PersonVO>>() {
        }).getResult().getClass());
        // 1.2.11 class com.alibaba.fastjson.JSONObject
        // 1.2.12 class com.alibaba.fastjson.JSONObject
        ptl(JSON.parseObject("{'result':{}}", WebResult.class).getResult().getClass());
    }

    private static void error2() {
        // 1.2.11 class com.alibaba.fastjson.JSONObject
        // 1.2.12 class com.alibaba.fastjson.JSONObject
        ptl(JSON.parseObject("{'result':{}}", new TypeReference<WebResult>() {
        }).getResult().getClass());
        // 1.2.11 java.lang.ClassCastException: com.alibaba.fastjson.JSONObject cannot be cast to PersonVO
        // 1.2.12 class PersonVO
        ptl(JSON.parseObject("{'result':{}}", new TypeReference<WebResult<PersonVO>>() {
        }).getResult().getClass());
    }

    private static void success3() {
        // 1.2.11 class com.alibaba.fastjson.JSONObject
        // 1.2.12 class com.alibaba.fastjson.JSONObject
        ptl(JSON.parseObject("{'result':{}}", new TypeReference<WebResult<Object>>() {
        }).getResult().getClass());
        // 1.2.11 class PersonVO
        // 1.2.12 class PersonVO
        ptl(JSON.parseObject("{'result':{}}", new TypeReference<WebResult<PersonVO>>() {
        }).getResult().getClass());
    }

    private static void success4() {
        // 1.2.11 class PersonVO
        // 1.2.12 class PersonVO
        ptl(JSON.parseObject("{'result':{}}", new TypeReference<WebResult<PersonVO>>() {
        }).getResult().getClass());
        // 1.2.11 class com.alibaba.fastjson.JSONObject
        // 1.2.12 class com.alibaba.fastjson.JSONObject
        ptl(JSON.parseObject("{'result':{}}", new TypeReference<WebResult<Object>>() {
        }).getResult().getClass());
    }

    public static void main(String[] args) {
        error1();
//        success1();

//        error2();

//        success3();

//        success4();

    }

    private static void ptl(Object o) {
        System.out.println(o);
    }
}
```


# Bug 修复版本

该 Bug 在 `1.2.11` 及其之前存在，在 `1.2.12` 及其之后已经修复

查看 Git 提交历史后发现，在 `8f8540825c95a8cf22636799906d8864366c0658` 这次提交之后进行修复，其上一次提交 `8f8540825c95a8cf22636799906d8864366c0658` 还有问题，修复的代码可[点击此处](https://github.com/alibaba/fastjson/commit/8f8540825c95a8cf22636799906d8864366c0658) 

# 为什么会出现 `ClassCastException`

可以通过反编译看出来，编译后的代码 JDK 自动加上强制类型转换。
因为传入 fastjson 的类型信息与实际返回的类型信息不符，所以会类型转换错误。

## 编译前
``` java 
private static void error1() {
    // 1.2.11 class com.alibaba.fastjson.JSONObject
    // 1.2.12 class com.alibaba.fastjson.JSONObject
    ptl(JSON.parseObject("{'result':{}}", WebResult.class).getResult().getClass());
    // 1.2.11 java.lang.ClassCastException: com.alibaba.fastjson.JSONObject cannot be cast to PersonVO
    // 1.2.12 class PersonVO
    ptl(JSON.parseObject("{'result':{}}", new TypeReference<WebResult<PersonVO>>() {
    }).getResult().getClass());
}
```

## 编译后

``` java 
private static void error1() {
    ptl(((WebResult)JSON.parseObject("{'result':{}}", WebResult.class)).getResult().getClass());
    ptl(((PersonVO)((WebResult)JSON.parseObject("{'result':{}}", new TypeReference<WebResult<PersonVO>>() {
    }, new Feature[0])).getResult()).getClass());
}
```

# 解决方案
1. 升级 fastjson 版本到 1.2.12+
2. 所有使用反序列化泛型类的地方，必须传入泛型信息

# Read More
- [FastJson 泛型转换踩坑](https://blog.csdn.net/ykdsg/article/details/50432494)
