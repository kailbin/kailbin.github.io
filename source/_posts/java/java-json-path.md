---

title: 使用 json-path 解析 json 数据
date: 2017-3-17
desc: 使用 json-path 解析 json 数据

tags: [java,json,工具]
categories: Java
---

JsonPath 表达式可以用 *类似 XPath 解析 XML 文档* 的方式来解析 JSON 结构。

> JsonPath expressions always refer to a JSON structure in the same way as XPath expression are used in combination with an XML document

<!--more-->

# 依赖

JsonPath 已加入 Maven 中央仓库，添加依赖

``` java
<dependency>
    <groupId>com.jayway.jsonpath</groupId>
    <artifactId>json-path</artifactId>
    <version>2.2.0</version>
</dependency>
```

# 表达式

| Operator | 描述 |Description |
|:---|:--- |:--- |
| `$`                        | 根节点                | The root element to query. This starts all path expressions.|
| `@`                         | 当前节点              | The current node being processed by a filter predicate.|
| `*`                        | 匹配任何一个数字和name | Wildcard. Available anywhere a name or numeric are required.| 
|`..`                        | 深度扫描              | Deep scan. Available anywhere a name is required.|
|`.<name>`                  |  选择name               | Dot-notated child |
|`[]`                       |迭代器标示，如数组下标    |   |
|`['<name>' (, '<name>')]`| 一个或者多个name          | Bracket-notated child or children | 
|`[<number> (, <number>)]`| 一个或者多个name          | Array index or indexes | 
|`[start:end]`         |数组切片运算符        |Array slice operator|
|`[?(<expression>)]`        |支持过滤操作|        	Filter expression. Expression must evaluate to a boolean value.|



# 测试数据

``` json
{
    "store":{
        "book":[
            {
                "category":"reference",
                "author":"Nigel Rees",
                "title":"Sayings of the Century",
                "price":8.95
            },
            {
                "category":"fiction",
                "author":"Evelyn Waugh",
                "title":"Sword of Honour",
                "price":12.99
            },
            {
                "category":"fiction",
                "author":"Herman Melville",
                "title":"Moby Dick",
                "isbn":"0-553-21311-3",
                "price":8.99
            },
            {
                "category":"fiction",
                "author":"J. R. R. Tolkien",
                "title":"The Lord of the Rings",
                "isbn":"0-395-19395-8",
                "price":22.99
            }
        ],
        "bicycle":{
            "color":"red",
            "price":19.95
        }
    },
    "expensive":10
}
```

# 测试

``` java
import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.jayway.jsonpath.Criteria.*;
import static com.jayway.jsonpath.Filter.*;

/**
 * Created by Kail on 2017/3/17.
 * Copyright(c) ttpai All Rights Reserved.
 */
public class JsonPathMain {

    static String readFile(String fileName) throws URISyntaxException, IOException {
        List<String> lines = Files.readAllLines(Paths.get(JsonPathMain.class.getResource(fileName).toURI()), Charset.forName("UTF-8"));
        StringBuilder jsonBuilder = new StringBuilder();
        for (String line : lines) {
            jsonBuilder.append(line);
        }
        return jsonBuilder.toString();
    }

    static void print(Object obj) {
        System.out.println();
        System.out.println(obj);
    }

    public static void main(String[] args) throws Exception {

        String json = readFile("store.json");

        // 获取所有书的作者:
        List<String> allAuthors = JsonPath.read(json, "$.store.book[*].author");
        print(allAuthors);

        // 获得第一本书的个作者
        String firstAuthor = JsonPath.read(json, "$.store.book[1].author");
        print(firstAuthor);

        // 过滤出来 category 为 reference 的书
        List<Object> bookCategoryFilter1 = JsonPath.read(json, "$.store.book[?(@.category == 'reference')]");
        List<Object> bookCategoryFilter2 = JsonPath.read(json, "$.store.book[?]", filter(where("category").is("reference")));
        print(bookCategoryFilter1);
        print(bookCategoryFilter2);

        // 过滤出来 price > 10 的书
        List<Object> bookPriceFilter1 = JsonPath.read(json, "$.store.book[?(@.price > 10)]");
        List<Object> bookPriceFilter2 = JsonPath.read(json, "$.store.book[?]", filter(where("price").gt(10)));
        print(bookPriceFilter1);
        print(bookPriceFilter2);

        // 查找 有isbn属性 的书
        List<Object> bookExistIsbn1 = JsonPath.read(json, "$.store.book[?(@.isbn)]");
        List<Object> bookExistIsbn2 = JsonPath.read(json, "$.store.book[?]", filter(where("isbn").exists(true)));
        print(bookExistIsbn1);
        print(bookExistIsbn2);

        // 过滤链
        Filter filter = filter(where("isbn").exists(true).and("category").in("fiction", "reference"));
        List<Object> chainedFilters = JsonPath.read(json, "$.store.book[?]", filter);
        print(chainedFilters);

        // 所有的Price字段
        List<Double> allPrices = JsonPath.read(json, "$..price");
        print(allPrices);

        // 编译Json
        DocumentContext documentContext = JsonPath.parse(json);
        // 编译 path
        JsonPath allBooksPath = JsonPath.compile("$.store.book[*]");
        List<Object> allBooks = documentContext.read(allBooksPath);
        print(allBooks);
    }

}

```

# 函数


> Functions can be invoked at the tail end of a path - the input to a function is the output of the path expression. The function output is dictated by the function itself.

|函数 | 描述	|Description | 
|---:|:---|-- |
|	`min()`	| 最小值	| Provides the min value of an array of numbers	|	
|	`max()`	| 最大值	|Provides the max value of an array of numbers	|	
|	`avg()`	| 平均值	|Provides the average value of an array of numbers	|	
|	`stddev()`| 标准差=方差的算术平方根	|	Provides the standard deviation value of an array of numbers	|	
|	`length()`| 长度	|	Provides the length of an array|	

## 示例

``` json
{"numbers": [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ] }
```

|函数 | 描述 | 
|---:|:---|
|	`$..book.length()`	| 书的个数	|	
|	`$.numbers.max()`	| 数组的最大值	|	
|	`$.numbers.avg()`	| 数组的平均值	|	

> https://github.com/jayway/JsonPath/pull/167

# 过滤器操作

|Operator |	Description |
|---:|:---|
|`==`       |	left is equal to right (note that 1 is not equal to '1')|
|`!=`       |   left is not equal to right|
|`<`	    |   left is less than right|
|`<=`	    |   left is less or equal to right|
|`>`	    |   left is greater than right|
|`>=`	    |   left is greater than or equal to right|
|`=~`	    |   left matches regular expression `[?(@.name =~ /foo.*?/i)]`|
|`in`	    |   left exists in right `[?(@.size in ['S', 'M'])]`|
|`nin`      |	left does not exists in right|
|`size`	    |   size of left (array or string) should match right|
|`empty`	|   left (array or string) should be empty|

## 示例
- `[?(@.price < 10 && @.category == 'fiction')] `
- `[?(@.category == 'reference' || @.price > 10)]`

# 拓展阅读


> [JsonPath Github](https://github.com/jayway/JsonPath)
> [在线测试工具](http://jsonpath.herokuapp.com/)
> [json-path解析json方便可靠](https://www.oschina.net/code/snippet_273576_34427)
