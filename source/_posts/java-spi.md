---

title: Java SPI
date: 2016-12-13
desc: Java SPI

---

SPI的全名为Service Provider Interface，是针对厂商或者插件的，在`java.util.ServiceLoader`的文档里有比较详细的介绍。

系统里抽象的各个模块，往往有很多不同的实现方案，比如`jdbc`模块的方案等。为了实现在模块装配的时候，能不在程序里动态指明，这就需要一种服务发现机制。 

**java spi就是提供这样的一个机制：为某个接口寻找服务实现的机制。**

<!-- more -->

### 约定

服务的提供者 提供了服务接口的实现之后，在jar包的`META-INF/services/`目录里创建一个以**服务接口命名的文件**，**文件的内容**为实现接口的**全类名**。

### 如何使用
以 MySQL JDBC 驱动为例，添加 Maven 项目的依赖
```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.13</version>
</dependency>
```
mysql-connector-java-5.1.13.jar 的包内结构
```
mysql-connector-java-5.1.13.jar
|-- com.mysql.jdbc
|-- META-INF
|--|-- services
|--|--|-- java.sql.Driver
```

代码片段
```java
public class JdbcConnectTest {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        ServiceLoader<Driver> sl = ServiceLoader.load(java.sql.Driver.class); // 加载 java.sql.Driver 的实现类
        Iterator<Driver> iterator = sl.iterator();
        if (iterator.hasNext()) {
            Driver driver = iterator.next();
            System.out.println(driver.getClass().getName());
        }
    }
} 
```
输出 

    com.mysql.jdbc.Driver


### 使用SPI的方式连接数据库

完善以上代码
```java
public class JdbcConnectTest {  
  
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        ServiceLoader<Driver> sl = ServiceLoader.load(java.sql.Driver.class); // 加载 java.sql.Driver 的实现类
        Iterator<Driver> iterator = sl.iterator();
        if (iterator.hasNext()) {
            Driver driver = iterator.next();
            Properties properties = new Properties();
            properties.setProperty("useUnicode", "true");
            properties.setProperty("characterEncoding", "utf-8");
            properties.setProperty("user", "kail");
            properties.setProperty("password", "123456");
            // 连接到本地的mysql库
            try (Connection connect = driver.connect("jdbc:mysql:///mysql", properties)) {
                try (Statement statement = connect.createStatement()) {
                    // 查询 db 表
                    try (ResultSet resultSet = statement.executeQuery("SELECT * FROM  db")) {
                        while (resultSet.next()) {
                            String dbName = resultSet.getString(2);
                            System.out.println(dbName); // 输出库名
                        }
                    }
                }
            }
        }
    }
}  
```


> [【java规范】Java spi机制浅谈](http://singleant.iteye.com/blog/1497259)  
> 
> [java中的SPI机制](http://www.cnblogs.com/javaee6/p/3714719.html)  


