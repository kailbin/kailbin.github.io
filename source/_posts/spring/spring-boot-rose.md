---------------
title: Spring Boot 集成 paoding-rose-jade
date: 2017-03-14
desc: Spring Boot 集成 paoding-rose-jade
tags: [Java,Spring,Spring Boot] 
categories: Spring
---------------

> paoding-rose 是一个开源框架, 它可以简化 web 应用和 RDMS 应用的开发。

因为公司的原因，所以才有幸接触到了这个小众且‘古老’的框架。其依赖 Spring 的一套体系，主要对 Controller 和 DAO 层提供支持。

Controller 跟 Spring MVC 比较类似，但是相对Spring MVC来说功能稍弱一点，自我感觉可以完全被 Spring MVC 替代掉；
DAO 又跟 MyBatis 比较类似，相对轻量一些，实际上是对 JdbcTemplate 的封装，并添加注解/SQL自定义标签等支持，使用起来很方便。

paoding-rose 跟其他主流框架如此类似，为什么还会选择它呢？ 
个人感觉可能是跟它的很多“强制限制”有关，paoding-rose 规定 Controller 层 类必须以`Controller`结尾，必须写在 `controller`包下面；
DAO 层都是接口，命名必须以 `DAO` 结尾，必须写在 `dao` 包下面等等一些强制的要求，否者项目就跑不起来。
跟`约定大于配置`这个概念有点类似，但是其又不提供配置，必须按照要求来，这些强制的编码要求使开发相对来说规范起来，对后期维护是有益的。

这里在 Spring Boot 中引入 paoding-rose-jade(DAO支持)模块，本文作为记录。

<!--more-->

# Maven 依赖

这里把 Spring 的老版本排除掉。
``` xml
<dependency>
    <groupId>com.54chen</groupId>
    <artifactId>paoding-rose-jade</artifactId>
    <version>1.1</version>
    <exclusions>
        <exclusion>
            <artifactId>spring</artifactId>
            <groupId>org.springframework</groupId>
        </exclusion>
        <exclusion>
            <artifactId>commons-logging</artifactId>
            <groupId>commons-logging</groupId>
        </exclusion>
    </exclusions>
</dependency>
```

# 加载  paoding-rose-jade

创建类 继承 `net.paoding.rose.jade.context.spring.JadeBeanFactoryPostProcessor` 即可。

> Spring容器完成其内部的标准初始化工作后将调用本处理器，识别符合Jade规范的 DAO 接口并将之配置为Spring容器的Bean定义，加入到Spring容器中

``` java
import net.paoding.rose.jade.context.spring.JadeBeanFactoryPostProcessor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JadeBootBeanFactoryPostProcessor extends JadeBeanFactoryPostProcessor {

}
```

# 常见例子与写法


``` java
import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;

import java.util.List;

@DAO
public interface UserDAO {

    @SQL("select id,user_name,age from t_user where 1=1" +
            " #if(null!=:u.id){ and id=:u.id }" +
            " #if(null!=:u.userName && ''!=:u.userName){ and user_name=:u.userName }" +
            " order by id")
    List<UserVO> selectUsers(@SQLParam("u") UserVO user);
    
    
    @ReturnGeneratedKeys
    @SQL("insert into t_user (user_name,age) values (:u.userName,:u.age)")
    Integer insertUser(@SQLParam("u") UserVO user);
    
    
    @SQL("update t_user set " +
            " #if(null!=:u.userName && ''!=:u.userName){ user_name=:u.userName, }" +
            " #if(null!=:u.age){ age=:u.age, }" +
            " id=id " +
            " where id=:u.id " )
    Integer updateUser(@SQLParam("u") UserVO user);
    
}
```
以上是一个简单 DAO 代码示例，见名知意，这里不做过多解释。
关键点如下：
- 其要求SQL字段等名必须符合规范，全部小写或者大写，使用下划线分割，其内部对 `下划线分割的数据库字段名`和`驼峰命名方式的Java对象字段`进行对应
- 不支持嵌套 Mapping，即不支持对嵌套的Java对象进行查询赋值，仅支持单层Java对象映射，其深意在于 SQL 中应尽可能的减少 `JOIN` 的使用，尽可能把连表查询的需求，放到Service里面进行处理
- 重要精力会转移到SQL优化方向

其它 批量处理，分库分表的功能详见 拓展阅读中的 rose 手册。

# DAO 测试

``` java
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AppMain.class) // AppMain 是 Spring Boot 的启动入口
public class BaseBootTest {
}
```

``` java
public class UserDAOTest extends BaseBootTest {

    @Autowired
    private UserDAO userDAO;

    @Test
    public void testSelectUsers() throws Exception {
        UserVO user = new UserVO();
        user.setId(123); // 查询 id 为 123 的用户 
        
        userDAO.selectUsers(user);
    }
}
```



# 拓展阅读
> [rose手册](http://www.54chen.com/rose.html)
> [paoding-rose](https://github.com/paoding-code/paoding-rose)