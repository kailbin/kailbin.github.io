---------------
title: 自定义 Spring RestTemplate Message Converters
date: 2017-06-07
desc: 自定义 Spring RestTemplate Message Converters
tags: [Java,Spring,RestTemplate] 
categories: Spring
---------------

`org.springframework.core.ParameterizedTypeReference` 是 Spring 3.2 之后新增的类，使用 `RestTemplate` 请求数据反序列化的时候，通过该类可以**保留泛型**。

Spring 对 Jackson 是默认支持的，当 classpath 下有 Jackson 的时候，Spring 会使用 Jackson 进行反序列化，但是配合 `ParameterizedTypeReference` 的时候有个"bug"，假如*请求到的 json 数据里面的字段比 目标 bean 里面的字段多*，就会报一下的异常：
```
Caused by: com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException: Unrecognized field "..."
.... 省略若干
```
这里只测试了 `3.2.18.RELEASE`、`4.0.7.RELEASE` 、`4.2.9.RELEASE` 都存在该问题，到目前为止Spring4的最后一个版本`4.3.9.RELEASE` 则没有该问题。

如果公司的Spring 版本在 3.2 ~ 4.2 之间徘徊，并且不考虑升级Spring版本的话，接下来内容可能会对您有用。


<!--more-->

# 默认的 Message Converters

通过 RestTemplate 的构造方法可以大致了解一下 其默认的 Message Converters。

``` java
public RestTemplate() {
    this.messageConverters.add(new ByteArrayHttpMessageConverter()); // application/octet-stream
    this.messageConverters.add(new StringHttpMessageConverter()); // text/plain
    this.messageConverters.add(new ResourceHttpMessageConverter()); 
    this.messageConverters.add(new SourceHttpMessageConverter<Source>()); // application/xml
    this.messageConverters.add(new AllEncompassingFormHttpMessageConverter()); // application/x-www-form-urlencoded

    if (romePresent) {
        this.messageConverters.add(new AtomFeedHttpMessageConverter());
        this.messageConverters.add(new RssChannelHttpMessageConverter());
    }
    if (jaxb2Present) {
        this.messageConverters.add(new Jaxb2RootElementHttpMessageConverter());
    }
    if (jackson2Present) {
        this.messageConverters.add(new MappingJackson2HttpMessageConverter()); // GenericHttpMessageConverter 接口的实现，支持泛型反序列化
    }
    else if (jacksonPresent) {
        this.messageConverters.add(new org.springframework.http.converter.json.MappingJacksonHttpMessageConverter());
    }
}
```
Message Convert(消息格式转换) 的时候会从前到后，通过`Content-Type`来匹配使用哪个转换器来转换
``` java
for (HttpMessageConverter<?> messageConverter : this.messageConverters) {
    // 是否支持泛型 Convert，通过 ParameterizedTypeReference 的匿名子类来实现
    if (messageConverter instanceof GenericHttpMessageConverter) {
        GenericHttpMessageConverter<?> genericMessageConverter = (GenericHttpMessageConverter<?>) messageConverter;
        if (genericMessageConverter.canRead(this.responseType, null, contentType)) {
            ...
        }
    }
    // 非泛型的 Convert，类似于 List.class 、String.class 等
    if (this.responseClass != null) {
        if (messageConverter.canRead(this.responseClass, contentType)) {
            ...
        }
    }
}
```
接下来会通过  `new ParameterizedTypeReference<List<GithubGistVO>>() {};` 来保留泛型，MappingJackson2HttpMessageConverter 前面的 Converter 都没有实现 GenericHttpMessageConverter，所以会通过 MappingJackson2HttpMessageConverter 来反序列化请求到的json数据。


# 自定义 Jackson 配置

json数据与Bean字段不一致报错，实际上是 Jackson 的配置问题，自定义 Jackson 配置，覆盖 Spring 的默认配置即可。

创建 MyObjectMapper 继承 ObjectMapper：

``` java
package xyz.kail.blog.test.resttemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class MyObjectMapper extends ObjectMapper {

    public MyObjectMapper() {
        this.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // 空对象不报错
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 出现未知的属性不报错
    }
}  
```
Spring 配置
```xml
<bean id="mappingJackson2HttpMessageConverter" class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter">
    <property name="objectMapper">
        <bean class="xyz.kail.blog.test.resttemplate.MyObjectMapper"/>
    </property>
</bean>
```

# 修改 RestTemplate Message Converters
``` xml
<bean id="restTemplate" class="org.springframework.web.client.RestTemplate">
    <constructor-arg ref="httpClientFactory"/>
    <property name="messageConverters">
        <list>
            <ref bean="mappingJackson2HttpMessageConverter"/>
        </list>
    </property>
</bean>
```
上面这个配置（RestTemplate.setMessageConverters）实际上存在一个问题，除非把所默认的MessageConverters再配置一遍，否则`setMessageConverters(List<HttpMessageConverter<?>> messageConverters)` 实际上会覆盖掉所有默认MessageConverters。

如何添加自定义的 MessageConverter 而不清除默认的MessageConverters呢？这里创建 MyRestTemplate 继承 RestTemplate：
``` java
package xyz.kail.blog.test.resttemplate;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class MyRestTemplate extends RestTemplate {

    public MyRestTemplate() {
    }

    public MyRestTemplate(ClientHttpRequestFactory requestFactory) {
        super(requestFactory);
    }

    @Override
    public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        Assert.notEmpty(messageConverters, "'messageConverters' must not be empty");
        List<HttpMessageConverter<?>> defaultConverters = super.getMessageConverters();

        if (defaultConverters != messageConverters) {
            defaultConverters.addAll(0, messageConverters);
        }
    }
}
```
这里把新的自定义的 MessageConverters 放到默认的前面，如果`Content-Type`是`application/json`,会先匹配到自定的MessageConverters。

# 国产的 fastjson 

如果确定了系统中的交互只是 json的形式，实际上覆盖掉默认的也未尝不可。这里使用 fastjson 作为 MessageConverter：
``` xml
<bean id="restTemplate" class="org.springframework.web.client.RestTemplate">
    <constructor-arg ref="httpClientFactory"/>
    <property name="messageConverters">
        <list>
             <bean class="com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter"/>
        </list>
    </property>
</bean>
```
fastjson 的序列化配置这里就不说，相比 jackson 的配置要简单许多，实际上fastjson的默认配置已经很符合中国国情了。

# RestTemplate 使用
``` java
public class Main {

    private static final String URL_STR = "https://api.github.com/gists/public";

    private static RestTemplate getRestTemplate() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("app-rest.xml");
        return context.getBean(RestTemplate.class);
    }

    public static void main(String[] args) throws IOException {
        ParameterizedTypeReference<List<GithubGistVO>> pt = new ParameterizedTypeReference<List<GithubGistVO>>() {
        };
        ResponseEntity<List<GithubGistVO>> responseEntity = getRestTemplate().exchange(URL_STR, HttpMethod.GET, null, pt);
        List<GithubGistVO> githubGists = responseEntity.getBody();
        System.out.println(githubGists);
    }
}
```

``` java
public class GithubGistVO {

    private String id;
    private String url;
    private String description;
    private String commentsUrl;

    ... 省略 getter setter
}
```




# 拓展阅读

> [28.10.1 RestTemplate](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/remoting.html#rest-resttemplate)
>
> [28.10.2 HTTP Message Conversion](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/remoting.html#rest-message-conversion)