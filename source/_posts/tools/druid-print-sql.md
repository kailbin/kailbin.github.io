---
title: Druid 打印可执行 SQL
tags:
  - Java
categories:
  - Tools
date: 2019-05-02
id: tools/druid-print-sql
---



使用 ORM 框架的时候，为了调试，常有打印 SQL 的需求，大部分打印出来的 SQL 带`?`（PreparedStatement）。这里记录一下在使用 Druid 连接池的时候，使用 Druid 自带的功能 打印 可执行的SQL。



<!-- more -->



## Maven 依赖

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>6.0.6</version>
</dependency>

<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.1.16</version>
</dependency>

<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>1.7.26</version>
</dependency>
```



## 示例代码

主要关注代码注释

```java
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.util.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Properties;

public class DruidPrintSqlMain {

    static {
        // slf4j-simple , 日志级别设置为 Debug
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
    }

    public static void main(String[] args) throws Exception {

        /*
         * LoggerName
         *
         * "druid.sql.DataSource";
         * "druid.sql.Connection";
         * "druid.sql.Statement";
         * "druid.sql.ResultSet";
         */
        Slf4jLogFilter slf4jLogFilter = new Slf4jLogFilter();
        // 是否 打印Connection 日志
        slf4jLogFilter.setConnectionLogEnabled(false); // 默认 true


        // Statement 日志
        slf4jLogFilter.setStatementLogEnabled(true);  // 默认 true
        // 是否 打印PreparedStatement语句
        slf4jLogFilter.setStatementPrepareAfterLogEnabled(false);  // 默认 true
        // 是否 打印PreparedStatement语句参数和参数类型
        slf4jLogFilter.setStatementParameterSetLogEnabled(false);  // 默认 true
        // ❤❤❤ 是否 打印Statement可执行语句
        slf4jLogFilter.setStatementExecutableSqlLogEnable(true);  // ❤ 默认 false
        // ❤❤❤ 是否 打印执行耗时
        slf4jLogFilter.setStatementExecuteQueryAfterLogEnabled(false); // 默认 true
        // 是否 打印 Statement close 日志
        slf4jLogFilter.setStatementCloseAfterLogEnabled(false); // 默认 true

        // 是否 打印 执行结果
        slf4jLogFilter.setResultSetLogEnabled(false);  // 默认 true


        /*
         * Druid 配置
         */
        Properties config = new Properties();
        // Loading class `com.mysql.jdbc.Driver'. This is deprecated.
        // The new driver class is `com.mysql.cj.jdbc.Driver'.
        // The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.
        // config.setProperty(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, "com.mysql.jdbc.Driver");
        config.setProperty(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, "com.mysql.cj.jdbc.Driver");
        config.setProperty(DruidDataSourceFactory.PROP_URL, "jdbc:mysql:///test");
        config.setProperty(DruidDataSourceFactory.PROP_USERNAME, "xxx");
        config.setProperty(DruidDataSourceFactory.PROP_PASSWORD, "xxx");

        DruidDataSource dataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(config);
        // 设置过滤器
        dataSource.setProxyFilters(Collections.singletonList(slf4jLogFilter));


        /*
         * JDBC 操作 -- 无需关注
         */
        Connection conn = dataSource.getConnection();

        PreparedStatement preparedStatement = conn.prepareStatement("select * from T_TEST_ where ID = ?");
        preparedStatement.setLong(1, 613290L);

        ResultSet resultSet = preparedStatement.executeQuery();

        JdbcUtils.close(resultSet);
        JdbcUtils.close(preparedStatement);
        JdbcUtils.close(conn);

    }
}
```

> 输出
>
> ```bash
> [main] DEBUG druid.sql.Statement - {conn-10001, pstmt-20000} executed. select *
> from T_TEST_
> where ID = 613290
> ```

## XML 配置示例

> 详见官方文档： [配置_LogFilter](https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_LogFilter)

```xml
<bean id="druidLogFilter" class="com.alibaba.druid.filter.logging.Slf4jLogFilter">
    <!-- 开发打印可执行的 SQL 语句 -->
    <property name="statementExecutableSqlLogEnable" value="true"/>
</bean>


<bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource" 
      init-method="init" 
      destroy-method="close">
    ...
    <property name="proxyFilters">
        <list>
            <ref bean="druidLogFilterr"/>
        </list>
    </property>
</bean>
```

## 注意日志级别

注意一下 LoggerName 的 日志级别要设置为 Debug

```java
// 数据源相关
druid.sql.DataSource
// 连接相关
druid.sql.Connection
// 语句相关
druid.sql.Statement
// 执行结果相关
druid.sql.ResultSet
```



## 如何获取可执行SQL

- JDK 中 JDBC 规范只提供接口（如 PreparedStatement 接口），并未提供获取可执行SQL的接口
- JDBC 规范的实现由各大数据库厂商实现，包括 预查询语句的 SQL 拼接 可执行语句也是由各大厂商实现

### MySQL JDBC 驱动示例

```java
Class.forName("com.mysql.jdbc.Driver");
Connection conn = DriverManager.getConnection("jdbc:mysql:///test", "root", "1723");
PreparedStatement preparedStatement = conn.prepareStatement("select * from T_TEST_ where ID = ?");
preparedStatement.setLong(1, 613290L);

// com.mysql.cj.jdbc.PreparedStatement@4157f54e: select * from T_TEST_ where ID = 613290
System.out.println(preparedStatement.toString());

if (preparedStatement instanceof com.mysql.cj.jdbc.PreparedStatement){
    com.mysql.cj.jdbc.PreparedStatement mysqlPreparedStatement = (com.mysql.cj.jdbc.PreparedStatement) preparedStatement;
    String sql = mysqlPreparedStatement.asSql();
    // select * from T_TEST_ where ID = 613290
    System.out.println(sql);

    String preparedSql = mysqlPreparedStatement.getPreparedSql();
    // select * from T_TEST_ where ID = ?
    System.out.println(preparedSql);

}
```



### Druid 实现方式

Druid 自己实现了 PreparedStatement 参数的拼接

```java
String sql = SQLUtils.format(
        "select * from asd where id = ? and birthday = ?",
        com.alibaba.druid.util.JdbcConstants.MYSQL,
        Arrays.asList(1L, new Date())
);

/*
 * SELECT *
 * FROM asd
 * WHERE id = 1
 * 	AND birthday = '2019-05-02'
 */
System.out.println(sql);
```



## Read More

- 官方文档
  - [内置Filter的别名](https://github.com/alibaba/druid/wiki/%E5%86%85%E7%BD%AEFilter%E7%9A%84%E5%88%AB%E5%90%8D)
  - [Filter配置](https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_Filter%E9%85%8D%E7%BD%AE)
  - [LogFilter](https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_LogFilter)