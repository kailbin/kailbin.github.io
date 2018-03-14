---
title: 使用 maven-assembly-plugin 打包
tags:
  - Java
  - Maven
categories:
  - Java
date: 2018-03-14
---

maven-assembly-plugin 可以用来帮助打包，比如说打出一个什么类型的包，包里包括哪些内容等等。

<!-- more -->

## 默认的Descriptor摆放位置

``` text
my-assembly-descriptor
    +-- src
    |   `-- main
    |       `-- resources
    |           `-- assemblies
    |               `-- pack-assembly.xml
    `-- pom.xml
```

> [Sharing Assembly Descriptors](http://maven.apache.org/plugins/maven-assembly-plugin/examples/sharing-descriptors.html)


## 内置的Assembly Descriptor

默认情况下，`maven-assembly-plugin` 内置了几个可以用的 `assembly descriptor`： `bin`、`jar-with-dependencies`、 `project` 、 `src`。


这几个描述文件内容可以在 `maven-assembly-plugin` 的 jar 包里找到，

``` text
maven-assembly-plugin-3.1.0.jar
    +-- assemblies
    |   `-- bin.xml
    |   `-- jar-with-dependencies.xml
    |   `-- project.xml
    |   `-- src.xml
    `-- META-INF
    `-- org.apache.maven.plugins.assembly
```

可参考 内置的 Descriptor，自定义 Descriptor，放到正确的位置即可，`descriptorRef` 是自定义的文件名。




## 示例

### pom.xml

```  xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>3.1.0</version>
    <dependencies>
        <dependency>
            <groupId>${groupId}</groupId>
            <artifactId>${artifactId}</artifactId>
            <version>${version}</version>
        </dependency>
    </dependencies>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
            <configuration>
                <descriptorRefs>
                    <!-- 类似于 maven-shade-plugin 的打包方式，所有依赖会被解压后，放入同一个jar -->
                    <!-- <descriptorRef>jar-with-dependencies</descriptorRef>-->

                    <!-- 把 src 下文源代码，压缩成 tar.bz2/tar.gz/zip 三种格式的文件 -->
                    <!--<descriptorRef>src</descriptorRef>-->
                    <!-- 把编译后的文件（target/class）目录，压缩成 tar.bz2/tar.gz/zip 三种格式的文件-->
                    <!--<descriptorRef>bin</descriptorRef>-->
                    <!-- 把整个项目，压缩成 tar.bz2/tar.gz/zip 三种格式的文件 -->
                    <!--<descriptorRef>project</descriptorRef>-->
                    
                    <!-- 自定义 descriptor -->
                    <descriptorRef>pack-assembly</descriptorRef>
                </descriptorRefs>
                <archive>
                    <manifest>
                        <mainClass>xyz.kail.demo.Main</mainClass>
                    </manifest>
                </archive>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### src/main/resources/assemblies/pack-assembly.xml

``` xml
<assembly xmlns="http://maven.apache.org/ASSEMBLY/2.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/ASSEMBLY/2.0.0 http://maven.apache.org/xsd/assembly-2.0.0.xsd">
    
    <id>pack-assembly</id>

    <formats>
        <format>jar</format>
    </formats>

    <includeBaseDirectory>false</includeBaseDirectory>
    
    <dependencySets>
        
        <dependencySet>
            <unpack>false</unpack>
            <scope>runtime</scope>
            <outputDirectory>lib</outputDirectory>
            <excludes>
                <exclude>${groupId}:${artifactId}</exclude>
            </excludes>
        </dependencySet>

        <dependencySet>
            <unpack>true</unpack>
            <includes>
                <include>${groupId}:${artifactId}</include>
            </includes>
        </dependencySet>

    </dependencySets>

</assembly>
```




## Read More

- [Apache Maven Assembly Plugin /  Examples](http://maven.apache.org/plugins/maven-assembly-plugin/examples/index.html)
- [assembly:single](http://maven.apache.org/plugins/maven-assembly-plugin/examples/single/using-container-descriptor-handlers.html)
- [maven-assembly-plugin的使用](https://www.cnblogs.com/f-zhao/p/6929814.html)