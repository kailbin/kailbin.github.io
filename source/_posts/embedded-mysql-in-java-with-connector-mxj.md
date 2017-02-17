---

title: 使用 MySQL Connector/MXJ 在 Java 中嵌入 MySQL 进行测试
date: 2017-02-05
desc: MySQL Connector/MXJ,嵌入 MySQL,测试

tags: [MySQL,测试]
---

MySQL Connector/MXJ 是一个可以通过 Java API 启动或者关闭嵌入式MySQL的工具。 
可以通过 MySQL JDBC driver 连接，通过 JDBC url 或者 使用`MysqldResource`编程的方式 控制MySQL 服务。 
如果需要测试SQL对MySQL语法或者特性依赖性比较强，可以尝试使用该方式进行测试，并不建议使用在生产环境中。

<!--more-->

### 一个例子

#### 创建一个 maven 项目，添加依赖

``` xml

<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.11</version>
</dependency>
  
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-mxj</artifactId>
    <version>5.0.12</version>
</dependency>

```

查看一依赖关系

``` bash
$ mvn dependency:tree

...
[INFO] +- junit:junit:jar:4.11:compile
[INFO] |  \- org.hamcrest:hamcrest-core:jar:1.3:compile
[INFO] \- mysql:mysql-connector-mxj:jar:5.0.12:compile
[INFO]    +- mysql:mysql-connector-mxj-db-files:jar:5.0.12:compile
[INFO]    \- mysql:mysql-connector-java:jar:5.1.17:compile
...

```

``` bash
$ mvn dependency:list
...
[INFO]    org.hamcrest:hamcrest-core:jar:1.3:compile
[INFO]    junit:junit:jar:4.11:compile
[INFO]    mysql:mysql-connector-mxj-db-files:jar:5.0.12:compile
[INFO]    mysql:mysql-connector-mxj:jar:5.0.12:compile
[INFO]    mysql:mysql-connector-java:jar:5.1.17:compile
...
```

#### BaseMySQLTest

``` java
import com.mysql.management.MysqldResource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by kail on 2017/2/5.
 */
public class BaseMySQLTest {
  
    private static final String MY_PROPERTIES = "my.properties"; // 服务配置写
    private static final File BASE_DIR = new File("/opt/data/mysql-embed/baseDir"); // MySQL 启动启动程序目录(mysql 和 mysqld)
    private static final File DATA_DIR = new File("/opt/data/mysql-embed/dataDir"); // MySQL 数据文件目录(表机构和数据)
  
    /**
     * 连接信息
     */
    private static final String driverName = "com.mysql.jdbc.Driver";
    private static final String url = "jdbc:mysql://127.0.0.1:3336/test?useUnicode=true&characterEncoding=UTF-8";
    private static final String username = "root";
    private static final String password = "";
  
  
    private static MysqldResource mysqlInstance = null;
    protected Connection connection = null;
  
    /**
     * 启动mysql 服务器
     */
    @BeforeClass
    public static void beforeClass() throws IOException {
        // 读取配置
        Properties props = new Properties();
        props.load(ClassLoader.getSystemResourceAsStream(MY_PROPERTIES));
  
        // 处理启动参数
        final Set<Object> keys = props.keySet();
        final Map<String, String> options = new HashMap<String, String>(keys.size());
        for (Object key : keys) {
            String val = props.getProperty(key.toString());
            if ("".equals(val)) {
                options.put(key.toString(), null);
            } else {
                options.put(key.toString(), val);
            }
        }
  
        // 启动 MySQL 资源实例
        mysqlInstance = new MysqldResource(BASE_DIR, DATA_DIR);
        if (!mysqlInstance.isRunning()) {
            mysqlInstance.start("Em_MySQL", options);
        }
    }
  
  
    /**
     * 获取 MySQL 链接
     */
    @Before
    public void before() throws ClassNotFoundException, SQLException {
        Class.forName(driverName);
        connection = DriverManager.getConnection(url, username, password);
    }
  
    /**
     * 关闭 MySQL 链接
     */
    @After
    public void after() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
  
  
    /**
     * 关闭 MySQL 服务实例
     */
    @AfterClass
    public static void afterClass() {
        if (null != mysqlInstance) {
            mysqlInstance.shutdown();
        }
    }
  
  
    public void println(Object obj) {
        System.out.println(obj);
    }
}

```

#### MySQLTest
``` java
import com.mysql.management.util.QueryUtil;
import org.junit.Before;
import org.junit.Test;
  
import java.sql.SQLException;
import java.util.List;
  
/**
 * Created by kail on 2017/2/5.
 */
public class MySQLTest extends BaseMySQLTest {
  
    private QueryUtil queryUtil;
  
    @Before
    public void before() throws SQLException, ClassNotFoundException {
        super.before();
  
        queryUtil = new QueryUtil(connection);
    }
  
    /**
     * 删除表
     */
    @Test
    public void testDropTable() {
        String sql = "DROP TABLE `T_TEST` ";
        queryUtil.execute(sql);
    }
  
    /**
     * 创建表
     */
    @Test
    public void testCreateTable() {
        String sql = "" +
                "CREATE TABLE `T_TEST` (\n" +
                "  `ID` int(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `UNAME` varchar(50) DEFAULT NULL,\n" +
                "  `AGE` int(11) DEFAULT NULL,\n" +
                "  `CREATE_TIME` datetime ,\n" +
                "  PRIMARY KEY (`ID`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
  
        queryUtil.execute(sql);
    }
  
    /**
     * 插入
     */
    @Test
    public void testInsert() {
        List returningKeys = queryUtil.executeUpdateReturningKeys("INSERT INTO T_TEST (UNAME,AGE,CREATE_TIME) VALUES (?,?,NOW())", new Object[]{"Kail", 24});
        println(returningKeys);
    }
  
    /**
     * 查询
     */
    @Test
    public void testSelect() {
        List query = queryUtil.executeQuery("SELECT * FROM T_TEST");
        println(query);
    }
  
    /**
     * 测试对函数的支持
     */
    @Test
    public void testMySQLFunction() {
        String[] functions = {
                "SELECT NOW()",
                "SELECT CURDATE()",
                "SELECT CURTIME()",
                "SELECT SUBDATE(CURDATE(),1)",
                "sELECT SUBDATE(NOW(),INTERVAL 1 HOUR)",
  
                "SELECT DATE_FORMAT(NOW(),'%W,%D %M %Y %r')",
                "SELECT DATE_FORMAT(19990330,'%Y-%m-%d')",
                "SELECT DATE_FORMAT(NOW(),'%h:%i %p')",
  
                "SELECT MD5('Kail')",
  
                "SELECT IFNULL(1,2)",
                "SELECT IFNULL(NULL,10)",
                "SELECT IF(1=2,1,2)",
  
                "SELECT CASE 9 WHEN 1 THEN 'a' WHEN 2 THEN 'b' ELSE 'N/A' END",
                "SELECT CASE WHEN (2+2)=4 THEN 'OK' WHEN(2+2)<>4 THEN 'not OK' END",
  
                "SELECT DATABASE()",
                "SELECT USER()",
                "SELECT VERSION()"
        };
  
        for (String func : functions) {
            println(String.format("函数 ：%-70s  结果：%s", func, queryUtil.queryForString(func)));
        }
  
    }
  
    /**
     * 如果不想每次都关闭MySQL服务器(不关闭会更快，避免每次都启动)
     * 可以注释掉 BaseMySQLTest.afterClass
     * 关闭的话，打开 BaseMySQLTest.afterClass 注释，执行一下
     */
    @Test
    public void noting(){
        println("only shutdown");
    }
}

```

#### my.properties 配置文件

该配置文件基本上和平常使用的 `my.ini` 文件内容一致，此处只留下一下三个配置，
 
``` 
port=3336
 
character-set-server=utf8
 
default-storage-engine=INNODB
```

### mysql:mysql-connector-mxj-db-files:jar:5.0.12

该jar包包含和各个平台的 `mysql`(client)和`mysqld`(server)，还含有一个MySQL 的帮助文档`com/mysql/management/MySQL_Help.txt`。

该文件有140，可以先下载，再手动安装到本地maven仓库里面，

jar 包内容大致如下：

``` bash
$ tar tvf .m2/repository/mysql/mysql-connector-mxj-db-files/5.0.12/mysql-connector-mxj-db-files-5.0.12.jar 
...
-rwxrwxrwx  0 0      0           0 Jan 21  2011 5-5-9/Win-x86/mysqld.exe
-rwxrwxrwx  0 0      0           0 Jun  2  2011 5-5-9/Win-x86/version.txt
-rwxrwxrwx  0 0      0           0 Jan 21  2011 5-5-9/Win-x86/mysql.exe
...
-rwxrwxrwx  0 0      0           0 Jun  1  2011 5-5-9/Mac_OS_X-i386/mysql
-rwxrwxrwx  0 0      0           0 Jun  2  2011 5-5-9/Mac_OS_X-i386/version.txt
-rwxrwxrwx  0 0      0           0 Jun  1  2011 5-5-9/Mac_OS_X-i386/mysqld
...
-rwxrwxrwx  0 0      0           0 Jun  2  2011 5-5-9/data_dir.jar
-rwxrwxrwx  0 0      0           0 Jun  2  2011 5-5-9/share_dir.jar
drwxrwxrwx  0 0      0           0 Jun  2  2011 5-5-9/Linux-i386/
-rwxrwxrwx  0 0      0           0 Jun  1  2011 5-5-9/Linux-i386/mysql
-rwxrwxrwx  0 0      0           0 Jun  2  2011 5-5-9/Linux-i386/version.txt
-rwxrwxrwx  0 0      0           0 Jun  1  2011 5-5-9/Linux-i386/mysqld
...
-rwxrwxrwx  0 0      0           0 Jun  2  2011 5-5-9/com/mysql/management/MySQL_Help.txt
...
-rwxrwxrwx  0 0      0           0 Oct  2  2011 5-5-9/Linux-x86_64/mysql
-rwxrwxrwx  0 0      0           0 Oct  2  2011 5-5-9/Linux-x86_64/version.txt
-rwxrwxrwx  0 0      0           0 Oct  2  2011 5-5-9/Linux-x86_64/mysqld
-rwxrwxrwx  0 0      0           0 Jun  2  2011 connector-mxj.properties
-rwxrwxrwx  0 0      0           0 Oct  2  2011 platform-map.properties
```

### baseDir 和 dataDir

启动之后会把 `mysql`和`mysqld` 拷贝到 baseDir 的 bin 目录下，
把 mysql 的基础数据文件(表机构和数据，在 mysql-connector-mxj-db-files-5.0.12.jar/5-5-9/data_dir.jar）拷贝到 dataDir 下。


实际上 MySQL Connector/MXJ 官方已经不再更新了，最新的版本是 5.0.12 。

估计可以替换通过 baseDir 中的 mysql 和 mysqld，来进行升级。 最新的 mxj-db-files 包含的mysql版本如下：。

``` bash
$ /opt/data/mysql-embed/baseDir/bin/mysql -V
/opt/data/mysql-embed/baseDir/bin/mysql  Ver 14.14 Distrib 5.5.9, for osx10.5 (i386) using readline 5.1
```

目录结构

``` bash
$ tree
.
├── baseDir
│   ├── bin
│   │   ├── mysql
│   │   └── mysqld
│   └── share
│       ├── charsets
│       │   ├── Index.xml
│       │   ├── README
│       │   ├── armscii8.xml
│       │   ├── ascii.xml
│       │   ├── cp1250.xml
│       │   ├── cp1251.xml
│       │   ├── cp1256.xml
│       │   ├── cp1257.xml
│       │   ├── cp850.xml
│       │   ├── cp852.xml
...
│           └── errmsg.sys
└── dataDir
    ├── ib_logfile0
    ├── ib_logfile1
    ├── ibdata1
    ├── mysql
 ...
    │   ├── time_zone_transition_type.frm
    │   ├── user.MYD
    │   ├── user.MYI
    │   └── user.frm
    ├── performance_schema
    │   ├── cond_instances.frm
    │   ├── db.opt
 ...
    │   └── threads.frm
    └── test
        └── T_TEST.frm

31 directories, 150 files

```



# 参考

> [MySQL Connector/MXJ (Archived Versions)](https://downloads.mysql.com/archives/c-mxj/)
>
> [Embedded MySQL in Java With Connector/MXJ and 64-bit Linux](http://blog.palominolabs.com/2011/10/03/embedded-mysql-on-java-with-connectormxj-and-64-bit-linux/)
>
> [Java中如何使用嵌入MySQL](https://my.oschina.net/eliyanfei/blog/779774#comment-list)
>
> [Maven Repository: mysql](http://mvnrepository.com/artifact/mysql)
>
