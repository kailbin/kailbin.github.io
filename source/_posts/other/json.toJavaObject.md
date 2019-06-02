---
title: fastjson 慎用 JSON.toJavaObject()
date: 2019-06-02
tags: [Bug]
categories:
  - Other
id: fastjson-bug-1.2.9
---


通过本文可以大致了解一下 fastjson JSON.toJavaObject() 方法的几个坑、限制 和 避免方式。

<!-- more -->

JSON 类 大致提供了三类方法
- toJSON... 对象转 json
- parse... json 转 对象
- **toJavaObject JSON 类转 POJO**

## toJavaObject 的实现

`toJavaObject(JSON, Class<T>)` 实际调用 `TypeUtils.cast(Object, Class<T>, ParserConfig)`，

参数是 Object 类型，理论上可以传任何参数，但是根据方法实现，只能传以下几类，否则会报 `can not cast to ...`
- Map 的实现
- Map 的实现集合
- 基本数据类型
- ...

所以不建议直接使用 `TypeUtils` 类，通过 `JSON.toJavaObject(JSON, Class<T>)` 调用的话，由于 `JSON` 类实现了 `Map` 接口，所以是符合 `TypeUtils.cast` 第一个参数的限制的

## toJavaObject 第二个 Class<T> 参数处理

如果通过 使用 `JSON.toJavaObject`，基本上会走到 `TypeUtils.cast(Object, Class<T>, ParserConfig)` 的以代码片段

``` java
...
// JSON obj, 实现 Map
// clazz 要转换成的类型
if (obj instanceof Map) {
    if (clazz == Map.class) {
        // 如果被转换的类型是 Map.class，直接返回，因为 JSON 本身就是 Map 的实现
        // 如果要转换成 纯正的 Map，可以通过 new HashMap<>(...) 进行包装
        return obj;
    } else {
        Map map = (Map)obj;
        return clazz == Object.class && !map.containsKey(JSON.DEFAULT_TYPE_KEY) ? 
                obj : 
                // 大部分场景最终会调用这个方法
                castToJavaBean((Map)obj, clazz, mapping);
    }
}
...
```

## TypeUtils.castToJavaBean(Map<String, Object>, Class<T>, ParserConfig)

该方法在 `1.2.9 之前` 和 `1.2.9 及其之后` 的实现上有所区别，对外表现的行为也不同，相同的代码

``` java
Map<String, Object> singletonMap = Collections.singletonMap("asd", 123);
System.out.println(singletonMap);
System.out.println(
        TypeUtils.castToJavaBean(
                singletonMap,
                HashMap.class,
                ParserConfig.getGlobalInstance()
        )
);
```


### 1.2.9 之前

输出空 HashMap
```
{asd=123}
{}
```
大致逻辑是实例化 `Class<T>` 后，找到其中的 get/set 方法，因为 HashMap 并没有相应 get/set 的方法，实例化之后并没有设置值。


### 1.2.9 及其以后

报错：
```
{asd=123}
Exception in thread "main" com.alibaba.fastjson.JSONException: can not get javaBeanDeserializer
	at com.alibaba.fastjson.util.TypeUtils.castToJavaBean(TypeUtils.java:917)
	at .....
Caused by: com.alibaba.fastjson.JSONException: can not get javaBeanDeserializer
	at com.alibaba.fastjson.util.TypeUtils.castToJavaBean(TypeUtils.java:912)
	... 1 more
```

大致逻辑是 根据 `Class<T>` 找到对应的 `ObjectDeserializer` 实现，之后只针对 `JavaBeanDeserializer` 和 `ASMJavaBeanDeserializer` 实现进行处理。

``` java
...
JavaBeanDeserializer javaBeanDeser = null;
ObjectDeserializer deserizer = config.getDeserializer(clazz);
if (deserizer instanceof JavaBeanDeserializer) {
    javaBeanDeser = (JavaBeanDeserializer) deserizer;
} else if (deserizer instanceof ASMJavaBeanDeserializer) {
    javaBeanDeser = ((ASMJavaBeanDeserializer) deserizer).getInnterSerializer();
}

if (javaBeanDeser == null) {
    throw new JSONException("can not get javaBeanDeserializer");
}

return (T) javaBeanDeser.createInstance(map, config);
```


> 个人感觉报错比 1.2.9 之前 的返回空对象要好，原因在于
>
> - 1.2.9 之前，返回值与预期想要的值是不一致的，如果不仔细测试，可能某些场景下很难发现这个问题，容易把问题带到生产环境
> - 1.2.9 及其以后，fastjson 提前把问题暴露出来，在开发节点即可发现，提早发现提早解决
> - 不过在升级 fastjson 的时候需要注意，由于行为上的不一致，可能原来隐藏的一个问题，通过异常的形式暴露的出来，有一定的风险

## 总结

- `toJavaObject(JSON, Class<T>)` 的第二个参数只能是
    - Map.class | 集合 （不能是 HashMap / JSONObject ... ）
    - 简单 Java 对象(POJO)类型 | 集合
    - ...

### 一个错误示例
``` java

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        String json = "{\"asd\":123}";

        // {"asd":123}
        System.out.println(JSON.toJavaObject(JSON.parseObject(json), Map.class));
        
        // Pojo{asd=123}
        System.out.println(JSON.toJavaObject(JSON.parseObject(json), Pojo.class));
        
        // Exception in thread "main" com.alibaba.fastjson.JSONException: can not get javaBeanDeserializer
        System.out.println(JSON.toJavaObject(JSON.parseObject(json), HashMap.class));
    }

    public static class Pojo {

        Integer asd;
        
        // 省略 get、set、toString
    }

}

```

### 错误示例拆解

`JSON.toJavaObject(JSON.parseObject(json), HashMap.class)` 拆解后
- `JSONObject jsonObject = JSON.parseObject(jsonStr);`
- `JSON json = jsonObject;`
- `JSON.toJavaObject(json, HashMap.class);`
    - `TypeUtils.cast(json, HashMap.class, ParserConfig)`
        - `TypeUtils.castToJavaBean(Map<String, Object>, HashMap.class, ParserConfig)`
            - `throw new JSONException("can not get javaBeanDeserializer");`


### 建议方式

实际上 先把 json 字符串穿换成  `JSON` 对象，再 case `JSON` 对象成 指定的 class，步骤稍微有点多余

建议直接使用以下两种方式
- `JSON.parseObject(jsonStr, HashMap.class)`;
- `JSON.parseObject(jsonStr, new TypeReference<HashMap<String, Integer>>() {})` 支持泛型

## Read More

- [记一次 fastjson 泛型反序列化的 Bug](/post/2018-09-02/other/fastjson-bug-1.2.11.html)