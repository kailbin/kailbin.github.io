---------------
title: Spring Boot 下 paoding-rose-jade 的多数据源配置
date: 2017-03-16
desc: Spring Boot 下 paoding-rose-jade 的多数据源配置
tags: [Java,Spring,Spring Boot] 
categories: Spring
---------------


对于给定的一个DAO接口，如 `com.mycompany.myapp.dao.UserDAO`，jade 为其配置数据源的默认方式如下：

- 如果存在 `id/name` 为 `jade.dataSource.com.mycompany.myapp.dao.UserDAO` 的数据源，则使用它作为这个DAO的数据源，否则逐级询问配置，直到顶一级包名：`jade.dataSource.com`
- 如果以上仍未能确定 `UserDAO` 的数据源，且 `UserDAO` 接口上的 `@DAO`的`catalog`属性非空（假设其值为myteam.myapp），则视myteam.myapp等同于package名，执行前一个步骤的问询，即按此顺序问询Spring容器的配置：`jade.dataSource.myteam.myapp.UserDAO`，...，`jade.dataSource.myteam`
- 如果以上仍未能确定UserDAO的数据源，则判断是否存在`id/name`为`jade.dataSource`、`dataSource`的数据源  
- 如果以上仍未能确定UserDAO的数据源，则最终就是没有数据源，运行时将会有异常抛出

以上默认数据源的寻找步骤摘录自 `JadeBeanFactoryPostProcessor.java` 的 类注释，[点击查看](https://github.com/paoding-code/paoding-rose/blob/master/paoding-rose-jade/src/main/java/net/paoding/rose/jade/context/spring/JadeBeanFactoryPostProcessor.java)。

<!--more-->

上篇文章 [Spring Boot 集成 paoding-rose-jade](/post/2017-03-14-spring-boot-rose.html) 中的默认单数据源配置，因为Spring容器中有名为 `dataSource` 的数据源，符合第三条的最后一条规则，所以可以正确的找到。

多数据源的情况，根据以上要求进行如下配置。

# jdbc.properties 文件

数据库配置单独抽出来，写入 jdbc.properties 文件，内容如下。

``` 
# 公共配置
common.datasource.driver-class-name=com.mysql.jdbc.Driver

# 数据库 test1
test1.datasource.driver-class-name=${common.datasource.driver-class-name}
test1.datasource.url=jdbc:mysql://localhost:3306/test1?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull
test1.datasource.username=username1
test1.datasource.password=password1

# 数据库 test2
test2.datasource.driver-class-name=${common.datasource.driver-class-name}
test2.datasource.url=jdbc:mysql://localhost:3306/test2?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull
test2.datasource.username=username2
test2.datasource.password=password2
```

# DataSourceConfig.java

创建多数据源

``` java
import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:jdbc.properties") //  从 jdbc.properties 加载配置信息
public class DataSourceConfig {

    @Primary // 指定默认主数据源
    @ConfigurationProperties(prefix = "test1.datasource")
    @Bean(name = "jade.dataSource.xyz.kail.boot.test.module1", initMethod = "init", destroyMethod = "close") // 模块1连接test1数据库
    public DataSource module1DataSource() {
        return new DruidDataSource();
    }

    // 数据源1 的事务管理器
    @Bean(name = "module1TxManager")
    public DataSourceTransactionManager module1TxManager() {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(module1DataSource());
        return transactionManager;
    }


    @ConfigurationProperties(prefix = "test2.datasource")
    @Bean(name = "jade.dataSource.xyz.kail.boot.test.module2", initMethod = "init", destroyMethod = "close") // 模块2连接test2数据库
    public DataSource module2DataSource() {
        return new DruidDataSource();
    }

    // 数据源2 的事务管理器
    @Bean(name = "module2TxManager")
    public DataSourceTransactionManager module2TxManager() {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(module2DataSource());
        return transactionManager;
    }
}
```
这样 `xyz.kail.boot.test.module1`和`xyz.kail.boot.test.module2` 包下或者子包的 DAO 会分别使用 两个数据源。
在 Service 层使用 DAO ，直接注入对用的 DAO 即可，不用对数据源进行额外的操作。 



# 启用事务

启用事务需要在入口的加入 `@EnableTransactionManagement` 注解，其等同于配置文件形式的 `<tx:annotation-driven />`。

``` java
@SpringBootApplication
@EnableTransactionManagement // 启用事务管理器
public class AppMain {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(AppMain.class, args);
    }
}
```

# 使用事务注解

这里需要注意的是，因为是多数据源，使用的时候要指定事务管理器的名称，`@Transactional(transactionManager = "module1TxManager")`,该注解一般加在 service 层的类或者方法上，操作哪个数据源使用哪个数据源的事务管理器。


# PS
按照 Spring Boot 的思想，一般一个 Spring Boot 应用应只负责一个模块的功能，尽量对一个领域的数据进行操作，多数据源的情况应尽量避免。


# 拓展阅读
> [Spring Boot Data Access](http://docs.spring.io/spring-boot/docs/1.5.2.RELEASE/reference/html/howto-data-access.html)
>
> [Spring Boot 集成 paoding-rose-jade](/post/2017-03-14-spring-boot-rose.html)



