---
title: Java8 Stream 快速入门
tags:
  - Java
categories:
  - Java
date: 2018-07-25
---


Java 8 中的 **Stream 是对集合（Collection）对象功能的增强**，它专注于对集合进行各种便利、高效的**聚合操作**（aggregate operation），或者**大批量数据操作** (bulk data operation)。

很多数据处理的场景不得不脱离 RDBMS，或者以底层返回的数据为基础进行更上层的数据统计。而以前 Java 的集合 API 中，仅仅有极少量的辅助型方法，更多的时候是程序员需要用 Iterator 来遍历集合，完成相关的聚合应用逻辑。这是一种远不够高效、笨拙的方法。

Stream API 借助于 Lambda 表达式，极大的提高编程效率。同时它提供**串行**和**并行**两种模式进行汇聚操作，并发模式能够充分利用多核处理器的优势，使用 **fork/join** 并行方式来拆分任务和加速处理过程。

<!-- more -->


Stream 不是一种数据结构，并不保存数据，其更像一种高级的迭代器（Iterator），阮一峰老师的 [图解 Monad](http://www.ruanyifeng.com/blog/2015/07/monad.html) 会更有助于理解 Stream。



# 创建 Stream

- 从 Stream 创建
    - `Stream.of()`
        - `IntStream`
        - `LongStream`
        - `DoubleStream`
    - `Stream.builder().add().build()`
    - 遍历1~3: `Stream.iterate(1, i -> ++i).limit(3)`
    - 连接两个Stream: `Stream.concat(Stream<? extends T> a, Stream<? extends T> b)`
    - `Stream.empty()`
- 从 Collection 和 数组 创建
    - `collection.stream()`
    - `collection.parallelStream()`
    - `Arrays.stream(T array)`
- 自己构建
    - `java.util.Spliterator`
- 其它
    - `java.io.BufferedReader.lines()`
    - `java.nio.file.Files.walk()`
    - `Random.ints()`
    - `BitSet.stream()`
    - `Pattern.splitAsStream(java.lang.CharSequence)`
    - `JarFile.stream()`


# 操作类型

## 中间转换操作(Intermediate)

一个流可以后面跟随零个或多个 intermediate 操作。其目的主要是打开流，做出某种程度的数据映射/过滤，然后返回一个新的流，交给下一个操作使用。这类操作都是惰性化的（`lazy`），就是说，仅仅调用到这类方法，并没有真正开始流的遍历

- 无状态操作
    - `map`: 处理迭代过程中的每个元素，每个输入元素，都按照规则转换成为另外**一个**元素
    - `flatMap`: 与Map的区别在于可以把多维集合压平成另一个Stream进行输出，可以理解为嵌套循环处理，如：在每个字母后面追加`,`并打印 `Stream.of("Hello", "World").flatMap(w -> Stream.of(w.split(""))).map(c -> c + ',').forEach(System.out::println);`
    - `filter`: 如果返回 true ，元素被留下进行后续操作
    - `distinct`: 滤重
    - `peek`: 与 map 的不同在于其没有返回值，可以处理每个元素，但是结果不会传递下去
    - `skip`: 跳过前几个元素
    - `limit`: 限制只处理几个元素
    - `parallel`: 标记后续操作为并行处理
    - `sequential`: 标记后续操作为串行处理
    - `unordered`: 无序的流
- 有状态操作
    - `sorted`: 排序，该操作会把并行流前后的无状态操作进行分割，降低并行性



> 一个操作可能会影响流的有序,比如`map`方法，它会用不同的值甚至类型替换流中的元素，所以输入元素的有序性已经变得没有意义了；但是对于`filter`方法来说，它只是丢弃掉一些值而已，输入元素的有序性还是保障的。
>
> 对于串行流，流有序与否不会影响其性能，只是会影响确定性(determinism)，无序流在多次执行的时候结果可能是不一样的。对于并行流，去掉有序这个约束可能会提供性能，比如`distinct`、`groupingBy`这些聚合操作。
>
> --鸟窝 《[Java Stream 详解](http://colobu.com/2016/03/02/Java-Stream/#%E6%8E%92%E5%BA%8F_Ordering)》




## Terminal 终止操作

一个流只能有一个 terminal 操作，当这个操作执行后，流就被使用“光”了，无法再被操作。所以这必定是流的最后一个操作。Terminal 操作的执行，才会真正开始流的遍历，并且会生成一个结果。

- `forEach`: 遍历
- `forEachOrdered`: 并不是排序后输出，而是在`parallel`情况下保证按照集合按照原始顺序输出， `forEach`在`parallel`无法保证顺序
- `toArray`: 结果存入数组
- `collect`: 结果存入集合
- `iterator`: 返回迭代器
- `min`: 求最小值
- `max`: 求最大值
- `count`: 计数
- `reduce`(`min`/`max`/`count`/... 都是特殊的 reduce)
    - 计数: `Stream.of(1, 2, 3).reduce(0, (reslut, b) -> ++reslut);`
    - 求最小: `Stream.of(1, 2, 3).reduce(Integer.MAX_VALUE, Math::min);`
    - 求最大: `Stream.of(1, 2, 3).reduce(Integer.MIN_VALUE, Math::max);`
    - 求和: `Stream.of(1, 2, 3).reduce(0, (reslut, b) -> reslut + b);`
    - 求平均: `DoubleStream.of(1, 2, 3).reduce(0, (reslut, b) -> (reslut + b / 2));`
    - 取第一: `Stream.of(1, 2, 3).reduce((reslut, b) -> reslut).get();`
    - 取最后: `Stream.of(1, 2, 3).reduce((reslut, b) -> b).get();`

## short-circuiting 短路操作

当 Stream 是一个无限大的集合的时候，就需要一个短路操作来使Stream 返回一个有限的结果集。

- `anyMatch`: 只要有一个元素符合条件就返回 true
- `allMatch`: 所有元素读符合条件才返回true
- `noneMatch`: 所有元素都不符合条件才返回true
- `findFirst`: 获取第一个元素
- `findAny`: 串行的情况还是返回第一个，并行的情况就不确定了，可能返回任意一个
- `limit`: 限制只处理几个元素

# 收集器（Collectors）

辅助对象，便于演示
``` java
public static class Person {
    public Long id;
    public String name;
    public Integer age;

    public Person(Long id, String name, Integer age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
    
    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
```
Stream
``` java
Stream<Person> personStream = Stream.of(
        new Person(1L, "张三", 18),
        new Person(2L, "张六", 18),
        new Person(3L, "李四", 23),
        new Person(4L, "赵五", 26)
);
```

## 常规用法
- `Collectors.toList()`: 结果存入 `ArrayList`
- `Collectors.toSet()`: 结果存入 `HashSet`
- `personStream.map(p -> p.name).collect(Collectors.joining(","))`: 姓名以`,`分割
- `Collectors.counting()`: 计数
- `Collectors.averagingInt(p -> p.age)`: 求平均年龄
    - `averagingInt`
    - `averagingLong`
    - `averagingDouble`
- `Collectors.summingInt(p -> p.age)`: 求和，等于`mapToInt(p -> p.age).sum()`
    - `summingInt`
    - `summingLong`
    - `summingDouble`
- `Collectors.summarizingInt(i -> i.age)`: 统计(`count`,`sum`,`min`,`average`,`max`)
    - `summarizingInt`
    - `summarizingLong`
    - `summarizingDouble`
- `Collectors.maxBy(Comparator.comparing(p -> p.age))`: 年龄最大的人
- `Collectors.minBy(Comparator.comparing(p -> p.age))`: 年龄最小的人

## toCollection
- `Collectors.toCollection(LinkedHashSet::new)`
- `Collectors.toCollection(TreeSet::new)`
- ...


## toMap / toConcurrentMap 

1. Key 是 age，Value 是 name，Key重复的情况下 Value用 `,` 分割

``` java
// {18=张三,张六, 23=李四, 26=赵五}
personStream.collect(Collectors.toMap(
        p -> p.age,
        p -> p.name,
        (result, current) -> result + "," + current
));
```

2. 获取ID与对象的映射关系

``` java
Map<Long, Person> idPersonMapping = personStream.collect(Collectors.toMap(
        p -> p.id, 
        p -> p
));
```
如果 Key 重复会报异常 `java.lang.IllegalStateException: Duplicate key xxx`



## 分组 (groupingBy / groupingByConcurrent)

``` java
// Map<Integer, List<Person>> 按照性别分组
personStream.collect(Collectors.groupingBy(p -> p.age))

// Map<String, List<Person>> 多字段分组
personStream.collect(Collectors.groupingBy(p -> p.id + p.name))
```

## 分区（partitioningBy）
满足条件的分为一组，不满足条件的分为另外一组
``` java
// Map<Boolean, List<Person>> 大于20分为一组，小于等于20的分为另外一组
personStream.collect(Collectors.partitioningBy(p -> p.age > 20))
```

## 其他

```java
Collector<Person, StringJoiner, String> personNameCollector =
    Collector.of(
            () -> new StringJoiner(" | "), // supplier
            (j, p) -> j.add(p.name),       // accumulator
            StringJoiner::merge,           // combiner
            StringJoiner::toString         // finisher
    );

String names = personStream.collect(personNameCollector);

System.out.println(names);  // 张三 | 张六 | 李四 | 赵五
```





# Read More

- [Java 8 中的 Streams API 详解](https://www.ibm.com/developerworks/cn/java/j-lo-java8streamapi/)
- [Java 8 Stream Tutorial](https://winterbe.com/posts/2014/07/31/java8-stream-tutorial-examples/)