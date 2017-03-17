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
