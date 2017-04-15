---------------
title: Spring @Profile 和 @Conditional 注解
date: 2017-04-15
desc: Spring Profile 和 Conditional 条件注解
tags: [Java,Spring] 
---------------

`@Profile` 和 `@Conditional` 都可以根据条件加载指定的Bean。`@Profile`主要使用场景是用来跟环境绑定，不同的环境加载不同的Bean；`@Conditional`可用定时更详细的加载条件。

<!--more-->

# 本文依赖的Spring版本
``` xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context-support</artifactId>
    <version>4.3.7.RELEASE</version>
</dependency>
```

# 配置类如下
``` java
package spring4.profile;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ProfileConfig {

    @Bean("text")
    @Profile("dev")
    public String devText() {
        return "dev";
    }

    @Bean("text")
    @Profile("rel")
    public String relText() {
        return "rel";
    }
}
```
`@Configuration` 注解标识这是一个配置类；  
`@Bean`注解定义一个托管给Spring的Bean，Bean 的 ID 都是text；  
`@Profile`将Bean与指定环境绑定，dev环境将得到"dev"字符串，rel环境将得到"rel"字符串；    

# 如何激活Profile
``` java
package spring4;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {  
  
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles("rel");
        context.scan("spring4");
        context.refresh();
    
        String text = context.getBean("text", String.class);
        System.out.println(text);
    }
}
```
这时候控制台将打印 rel，如果指定为dev(`context.getEnvironment().setActiveProfiles("dev");`)，将会打印dev。

# 没有指定Profile的Bean
在 ProfileConfig 类的**最上面（dev和rel之前）**配置一个没有指定任何 Profile 的Bean。
``` java
@Configuration
public class ProfileConfig {

    @Bean("text")
    public String defaultText() {
        return "no profile";
    }
    
    @Profile("dev")
    @Bean("text")
    ...
    @Profile("rel")
    @Bean("text")
    ...
}
```
这时候不管是激活dev还是rel，或者不激活任何Profile，都会打印 `no profile`，如何在不激活任何Profile的时候才输出**no profile**呢？ 这里先使用条件注解的方式。

# 条件注解
创建一个实现`Condition`接口的类，逻辑是如果没有激活任何profile，返回true。
``` java
package spring4.condition;
  
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
  
public class DefaultProfileCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        return conditionContext.getEnvironment().getActiveProfiles().length <= 0;
    }
}
```
在`defaultText()`方法上新增条件注解，如下
``` java
    @Bean("text")
    @Conditional(DefaultProfileCondition.class)
    public String defaultText() {
        return "no profile";
    }
```
这时候，只有在没有激活任何Profile，才会输出"no profile"。

# 默认Profile
以上为了引出条件注解，实际上绕了弯子，实现默认Profile实际上只需要`@Profile("default")`。

详见 `org.springframework.core.env.AbstractEnvironment` 类，里面定义了一个default常量，呵呵
``` java
	/**
	 * Return the set of reserved default profile names. This implementation returns
	 * {@value #RESERVED_DEFAULT_PROFILE_NAME}. Subclasses may override in order to
	 * customize the set of reserved names.
	 * @see #RESERVED_DEFAULT_PROFILE_NAME
	 * @see #doGetDefaultProfiles()
	 */
	protected Set<String> getReservedDefaultProfiles() {
		return Collections.singleton(RESERVED_DEFAULT_PROFILE_NAME);
	}
```

# PS
## 本文涉及到的注解对应的Spring 版本

|注解|Spring版本|
|-----|-----|
|@Configuration|3.0|
|@Bean|3.0|
|@Profile|3.1|
|@Conditional|4.0|

## 单元测试如何激活Profile
使用 `@ActiveProfiles("dev")` 即可


# 拓展阅读
> [Spring高级话题-条件注解-@Condition](http://blog.csdn.net/qq_26525215/article/details/53510156)  
> [Spring常用配置-Profile](http://blog.csdn.net/qq_26525215/article/details/53164481)
>  
> [@Profile](http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#beans-definition-profiles-java)  
> [Conditionally include @Configuration classes or @Bean methods](http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#beans-java-conditional)