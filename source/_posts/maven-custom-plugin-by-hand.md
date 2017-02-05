---

title: 手写 maven 插件
date: 2016-12-11
desc: 手写 Maven 插件,第一个Maven插件,Maven插件入门
---

本文主要通过编写一个插件来加深对`maven插件机制` `生命周期` `插件目标` `插件解析机制` 等概念的理解，插件本身并没有实际功能，只是输出一句 `Hello World`。

<!-- more -->

### 注意事项 和 概述
1. maven 插件的命名 `<name>-maven-plugin`

2. maven插件项目与一般maven项目，除了`<packaging></packaging>` 是 `maven-plugin` 以外，没有什么不同（`<packaging>maven-plugin</packaging>`）

3. maven插件项目的依赖
    ```xml
      <dependency>
                <groupId>org.apache.maven</groupId>
                <artifactId>maven-plugin-api</artifactId>
                <version>3.3.3</version>
      </dependency>
    ```

4. 编写maven插件可以使用两种风格的标注，直接`写在注释中(javadoc)`，或者使用JDK1.5+的`注解的形式`，如果使用注解的形式，需要使用依赖
    ```xml
        <dependency>
            <groupId>org.apache.maven.plugin-tools</groupId>
            <artifactId>maven-plugin-annotations</artifactId>
            <version>3.5</version>
        </dependency>
    ```
    以下两种声明目标的方式(`@goal print`和`@Mojo(name = "print")`)是一样的，
    因为使用的时候是通过jar包中的`plugin-help.xml`文件
    (`<name>-maven-plugin-<version>.jar\META-INF\maven\<groupId>\<name>-maven-plugin\plugin-help.xml`)
    来识别目标、参数等相关信息的。  
    如下展示了，声明 print 目标的两种方式。
    ```java
        @Mojo(name = "print")
        public class HelloMojo extends AbstractMojo {
          public void execute() throws MojoExecutionException, MojoFailureException {}
        }
    ```
    和
    ```java
        /**
         * @goal print
         */
        public class HelloMojo extends AbstractMojo {
          public void execute() throws MojoExecutionException, MojoFailureException{}
        }
    ```

5. 编写具体插件的时候需继承 AbstractMojo 类，实现其中的 `execute` 方法，MOJO即 Maven Ordinary Java Object [ 参见 POJO（Plain Ordinary Java Object）]


### 创建/生成一个Maven Plugin项目
1. 输入`mvn archetype:generate -DarchetypeCatalog=internal`
2. 这时候会提示，要使用哪个模版，如下，输入`3`,选择`maven-archetype-plugin`，也可以输入关键字进行搜索，然后进行选择
```
Choose archetype:
……
2: internal -> org.apache.maven.archetypes:maven-archetype-j2ee-simple (An archetype which contains a simplifed sample J2EE application.)
3: internal -> org.apache.maven.archetypes:maven-archetype-plugin (An archetype which contains a sample Maven plugin.)
4: internal -> org.apache.maven.archetypes:maven-archetype-plugin-site (An archetype which contains a sample Maven plugin site.
……
```
3. 接下来会提示输入 `<groupId>:com.kail.maven.plugin`、`<artifactId>:hello-maven-plugin`、`<version>:1.0-SNAPSHOT`、`<package>:com.kail.maven.plugin` 等信息。

4. 以上执行没问题的话 会生成一个 `hello-maven-plugin` 的Maven项目，Mojo 关键代码如下：

```java
package com.kail.maven.plugin;

@Mojo( name = "touch", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class MyMojo extends AbstractMojo{
    
    @Parameter( defaultValue = "${project.build.directory}", property = "outputDir", required = true )
    private File outputDirectory;

    public void execute()throws MojoExecutionException{
      ……
    }
}
```

### 稍微修改一下 MyMojo
```java
@Mojo(name = "print", defaultPhase = LifecyclePhase.CLEAN)
public class HelloMojo extends AbstractMojo {  
   
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;  
  
    @Parameter(defaultValue = "World", property = "maven.sayHello")
    private String text;  
  
    public void execute() throws MojoExecutionException {
        if (null != outputDirectory) {
            getLog().info("==" + outputDirectory.getAbsolutePath());
        } else {
            getLog().error("outputDirectory is null");
        }

        getLog().info("Hello " + text);
    }
}
```

### 发布到本地仓库
```bash
mvn clean install
```

### 使用插件

##### 直接使用

```bash
mvn com.kail.maven.plugin:hello-maven-plugin:1.0-SNAPSHOT:print 
```

输出

    ……
    [INFO] ------------------------------------------------------------------------
    [INFO] Building hello-maven-plugin Maven Plugin 1.0-SNAPSHOT
    [INFO] ------------------------------------------------------------------------
    ……
    
    [INFO] ==D:\Java\JetBrains\idea.2016\hello-maven-plugin\target
    [INFO] Hello World
    ……
  
   
使用参数`-Dmaven.sayHello=Kail` 设置变量 `text`
```bash
mvn com.kail.maven.plugin:hello-maven-plugin:1.0-SNAPSHOT:print -Dmaven.sayHello=Kail
```

输出

    ……
    [INFO] ------------------------------------------------------------------------
    [INFO] Building hello-maven-plugin Maven Plugin 1.0-SNAPSHOT
    [INFO] ------------------------------------------------------------------------
    ……
    
    [INFO] ==D:\Java\JetBrains\idea.2016\hello-maven-plugin\target
    [INFO] Hello Kail
    ……

##### 简写形式（目标前缀）

```bash
mvn hello:print 
```
```bash
mvn hello:print -Dmaven.sayHello=Kail
```

如果插件命名规范（如这种形式`<name>-maven-plugin`），并且pom中配置了对 `hello-maven-plugin` 插件的依赖，以上简写形式执行是没问题的。
如果pom中没有配置对 `hello-maven-plugin` 插件的依赖，也想通过以上简写形式执行的话，需要在 settings.xml 文件中配置一下信息。

```xml
<pluginGroups>
    <pluginGroup>com.kail.maven.plugin</pluginGroup>
</pluginGroups>
```

这时候maven就能自动补全，简写补全的搜索路径顺序如下：

1. .m2\repository\org\apache\maven\plugins\maven-metadata-central.xml

        <metadata>
          <plugins>
          ……
            <plugin>
              <name>Maven Archetype Plugin</name>
              <prefix>archetype</prefix>
              <artifactId>maven-archetype-plugin</artifactId>
            </plugin>
          ……
          </plugins>
        </metadata>
2. .m2\repository\org\codehaus\mojo\maven-metadata-central.xml

        <metadata>
          <plugins>
          ……
            <plugin>
              <name>Dependency Maven Plugin</name>
              <prefix>dependency</prefix>
              <artifactId>dependency-maven-plugin</artifactId>
            </plugin>
          ……
          </plugins>
        </metadata>
        
3. .m2\repository\com\kail\maven\plugin\maven-metadata-local.xml

        <metadata>
          <plugins>
          ……
            <plugin>
              <name>hello-maven-plugin Maven Plugin</name>
              <prefix>hello</prefix>
              <artifactId>hello-maven-plugin</artifactId>
            </plugin>
          ……
          </plugins>
        </metadata>

### 配置 & 目标绑定


```xml
<plugin>
    <groupId>com.kail.maven.plugin</groupId>
    <artifactId>hello-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <configuration>
        <text>Mr.Kail</text>
    </configuration>
    <executions>
        <execution>
            <!--多个 execution 的时候需要写ID，一个的话可以不写-->
            <id>id-clean-print</id>
            <!--目标`print`默认生命周期是clean [ @Mojo(name = "print", defaultPhase = LifecyclePhase.CLEAN) ]，所以可以不写phase-->
            <goals>
                <goal>print</goal>
            </goals>
        </execution>
        <execution>
            <!--多个 execution 的时候需要写ID，一个的话可以不写-->
            <id>id-validate-print</id>
            <!--将创建的目标`print`绑定到 `validate` 生命周期上-->
           <phase>validate</phase>
            <goals>
                <goal>print</goal>
            </goals>
            <configuration>
                <text>Mr.Kail(validate)</text>
            </configuration>
        </execution>
    </executions>
</plugin>
```

执行
```bash
mvn clean
```
输出

    ……
    [INFO] Hello Mr.Kail
    ……


执行
```bash
mvn validate
```
输出

    ……
    [INFO] Hello Mr.Kail(validate)
    ……
    
执行
```bash
mvn clean validate
```
输出

    ……
    [INFO] Hello Mr.Kail
    ……
    [INFO] Hello Mr.Kail(validate)
    ……
    

### 查询插件信息

```bash
mvn help:describe -Dplugin=com.kail.maven.plugin:hello-maven-plugin:1.0-SNAPSHOT
```


    Name: hello-maven-plugin Maven Plugin
    Description: (no description available)
    Group Id: com.kail.maven.plugin
    Artifact Id: hello-maven-plugin
    Version: 1.0-SNAPSHOT
    Goal Prefix: hello
    
    This plugin has 1 goal:
    
    hello:print
      Description: (no description available)
    
    For more information, run 'mvn help:describe [...] -Ddetail'

```bash
mvn help:describe -Dplugin=com.kail.maven.plugin:hello-maven-plugin:1.0-SNAPSHOT -Ddetail
```


    Name: hello-maven-plugin Maven Plugin
    Description: (no description available)
    Group Id: com.kail.maven.plugin
    Artifact Id: hello-maven-plugin
    Version: 1.0-SNAPSHOT
    Goal Prefix: hello
    
    This plugin has 1 goal:
    
    hello:print
      Description: (no description available)
      Implementation: com.kail.maven.plugin.HelloMojo
      Language: java
      Bound to phase: clean
    
      Available parameters:
    
        outputDirectory (Default: ${project.build.directory})
          Required: true
          User property: outputDir
          (no description available)
    
        text (Default: World)
          User property: maven.sayHello
          (no description available)


```bash
# 查询某个目标
mvn help:describe -Dplugin=com.kail.maven.plugin:hello-maven-plugin:1.0-SNAPSHOT -Ddetail -Dgoal=print
# 简写
mvn help:describe -Dplugin=hello -Ddetail
```
    
> 参考
> 
> [《Maven实战》 第7章\_生命周期和插件 和 第17章\_编写Maven插件)](https://item.jd.com/10476794.html)  
> 
> [maven开发java插件【译】](http://orchome.com/103)
> 
> [Your First Mojo](http://maven.apache.org/guides/plugin/guide-java-plugin-development.html)
>
> [Mojo API](http://maven.apache.org/developers/mojo-api-specification.html)
>
