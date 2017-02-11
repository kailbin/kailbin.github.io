---
title: Spring Schema 扩展
date: 2017-02-11
desc: spring标签,scheme,AbstractSingleBeanDefinitionParser,NamespaceHandlerSupport
---

Spring提供了可扩展Schema的支持，一般情况下，扩展Spring标签的意义并不大，但是对第三方工具来说，开发自己的标签供开发人员使用还是很有必要的。

首先就是有了语义，再者配置起来也相对方便，会减少一定的配置项。

常见的 Spring 的事物管理(`tx:`)、AOP(`aop:`)，还有淘宝的 Dubbo(`dubbo:`) 都有自己的标签库。

<!--more-->

### 依赖

``` xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>4.3.6.RELEASE</version>
</dependency>
```

### 从 `NamespaceHandlerSupport` 入手

可以通过 IDE 查看 `NamespaceHandlerSupport` 的继承关系，常见的如 `TxNamespaceHandler`、`MvcNamespaceHandler`、`AopNamespaceHandler` 等都是该类的子类。

这里我们也继承`NamespaceHandlerSupport`，该类是一个抽象类，需要实现其中的 `init()` 方法，该方法是在`NamespaceHandler`接口中定义的，注释如下：

``` java
public interface NamespaceHandler {

	/**
	 * Invoked by the {@link DefaultBeanDefinitionDocumentReader} after
	 * construction but before any custom elements are parsed.
	 *
	 * @see NamespaceHandlerSupport#registerBeanDefinitionParser(String, BeanDefinitionParser)
	 */
	void init();
	...
}
```

`@see NamespaceHandlerSupport#registerBeanDefinitionParser(String, BeanDefinitionParser)`，实现自己的 `PojoNamespaceHandler`，代码如下
``` java
package xyz.kail.blog.example;
  
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
  
/**
 * Created by kail on 2017/2/11.
 */
public class PojoNamespaceHandler extends NamespaceHandlerSupport {
  
    @Override
    public void init() {
        // pojo 即为 自定义的标签名
        registerBeanDefinitionParser("pojo",new PojoBeanParser());
    }
}

```

### 实现 PojoBeanParser


``` java
package xyz.kail.blog.example;
  
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;
  
import java.text.ParseException;
import java.text.SimpleDateFormat;
  
/**
 * Created by kail on 2017/2/11.
 */
public class PojoBeanParser extends AbstractSingleBeanDefinitionParser {
  
    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        // element 即为 pojo 元素对象，从中可以获得定义的属性
        String name = element.getAttribute("username");
        String birthday = element.getAttribute("birthday");
  
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        builder.addPropertyValue("username", name);
        try {
            builder.addPropertyValue("birthday", simpleDateFormat.parse(birthday));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    
    // 需要告诉 Spring 自定义标签生产的对象是什么
    @Override
    protected Class<?> getBeanClass(Element element) {
        return MySelfVO.class;
    }

}

```

##### MySelfVO 对象如下

``` java
package xyz.kail.blog.example;
  
import java.util.Date;
  
/**
 * Created by kail on 2017/2/11.
 */
public class MySelfVO {
  
    private String username;
  
    private Date birthday;
  
    ... 省略 get set
}

```

### 编写 Scheme

XML Schema 的作用是定义 XML 文档的合法构建模块，是W3C标准。

通俗来讲就是定义了一个XML文件应该有哪些元素、一个元素可以有哪些属性、属性必须是什么值、属性的类型等。

#### META-INF/kail-pojo.xsd 文件
``` xml
<?xml version="1.0" encoding="UTF-8"?>
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            xmlns="http://blog.kail.xyz/schema/kail"
            targetNamespace="http://blog.kail.xyz/schema/kail"
  
            elementFormDefault="qualified"
            attributeFormDefault="unqualified">
  
    <xsd:element name="pojo">
        <xsd:complexType>
            <xsd:attribute name="id" use="required"/>
            <xsd:attribute name="username" type="xsd:string"/>
            <xsd:attribute name="birthday" type="xsd:date"/>
        </xsd:complexType>
    </xsd:element>
  
</xsd:schema>  
```
该xsd 定义了一个 pojo 元素，包含 `id`、`username`、`birthday`三个属性，`id`是必填的，`username`是字符串类型的，`birthday`是日期类型的。
编写完 scheme 文件后，还需要在 `META-INF` 目录下写入 `spring.handlers`和`spring.schemas`两个文件，指明标签的实现和 scheme 文件位置，格式如下：

#### META-INF/spring.handlers
``` 
http\://blog.kail.xyz/schema/kail=xyz.kail.blog.example.PojoNamespaceHandler
```

#### META-INF/spring.schemas
``` 
http\://blog.kail.xyz/schema/kail/kail-pojo.xsd=META-INF/kail-pojo.xsd
```

### 测试

#### 添加依赖

``` xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-test</artifactId>
    <version>4.3.6.RELEASE</version>
</dependency>
  
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.12</version>
</dependency>
```

#### 测试类

``` java
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import xyz.kail.blog.example.MySelfVO;
  
/**
 * Created by kail on 2017/2/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/applicationContext.xml")
public class PojoNSTest {
  
    @Autowired
    private MySelfVO self1;
  
    @Autowired
    private MySelfVO self2;
  
    @Test
    public void testPojoNS() {
        System.out.println(self1.getUsername());
        System.out.println(self1.getBirthday());
    }
  
    @Test
    public void testSpringBean() {
        System.out.println(self2.getUsername());
        System.out.println(self2.getBirthday());
    }
}

```

#### applicationContext.xml 文件
``` xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:kail="http://blog.kail.xyz/schema/kail"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                http://www.springframework.org/schema/beans/spring-beans.xsd
                
                http://blog.kail.xyz/schema/kail
                http://blog.kail.xyz/schema/kail/kail-pojo.xsd">
  
    <!-- 自定义标签的形式 -->
    <kail:pojo id="self1" username="ykb" birthday="1992-07-23"/>
  
    <!-- 实例工厂的形式 -->
    <bean id="dateFormat" class="java.text.SimpleDateFormat">
        <constructor-arg value="yyyy-MM-dd"/>
    </bean>
   
    <bean id="self2" class="xyz.kail.blog.example.MySelfVO">
        <property name="username" value="dxp"/>
        <property name="birthday">
            <bean factory-bean="dateFormat" factory-method="parse">
                <constructor-arg value="1992-02-26"/>
            </bean>
        </property>
    </bean>
  
</beans>
```

### PS

本文讲的比较简单，复杂用法可以查看官方文档[42. Extensible XML authoring](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/xml-custom.html)，Scheme 语法可以查看 [w3c.cn Schema 教程](http://www.w3school.com.cn/schema/index.asp).

具体编写的时候可以查看 `NamespaceHandler` 接口的实现，根据其中的`init()` 方法顺藤摸瓜，查看其他优秀的第三方框架是如何实现的。

使用到未知的标签的时候也可以通过该方法，找到对应标签的实现，了解标签背后的原理。


# 参考

> 官方文档[42. Extensible XML authoring](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/xml-custom.html)
>
> [基于Spring可扩展Schema提供自定义配置支持](http://blog.csdn.net/cutesource/article/details/5864562)
>
> [w3c.cn Schema 教程](http://www.w3school.com.cn/schema/index.asp)
> [W3C Schema Specifications and Development](https://www.w3.org/XML/Schema#dev)