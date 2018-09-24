---
title: 使用 JaCoCo 生成代码覆盖率报告
date: 2018-09-24
tags: [QA]
categories: [QA]
id: jacoco-simple
---

JaCoCo是一个开源的覆盖率工具，可以作为Eclipse、IDEA插件使用，也可以通过Maven插件的方式使用，还可以使用其JavaAgent，实时生成Java程序的覆盖率报告等等。

很多第三方的工具提供了对JaCoCo的集成，如sonar、Jenkins等。

<!-- more -->

# 单元测试覆盖率

## Maven 属性
``` xml
<properties>
    <maven.compiler.encoding>UTF-8</maven.compiler.encoding>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
 
    <!-- 默认跳过测试 -->
    <maven.test.skip>true</maven.test.skip>
    <!-- 设置覆盖率报告位置，配置成属性的目的是便于命令行改变位置 -->
    <jacoco.report.path>${project.build.directory}/coverage-reports/jacoco-ut</jacoco.report.path>
</properties>
```

## 插件配置

``` xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.2</version>
    <executions>
        <!-- 准备指向Jacoco运行时Agent的属性，在测试执行之前传递给虚拟机参数 -->
        <execution>
            <id>pre-unit-test</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
            <configuration>
                <!-- 设置覆盖率数据文件路径 -->
                <destFile>${project.build.directory}/coverage-reports/jacoco-ut.exec</destFile>
            </configuration>
        </execution>
        <!-- 确保在单元测试执行之后生成覆盖率报告 -->
        <execution>
            <id>post-unit-test</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
            <configuration>
                <!-- 引用覆盖率文件的路径-->
                <dataFile>${project.build.directory}/coverage-reports/jacoco-ut.exec</dataFile>
                <!-- 设置覆盖率报告存放路径. -->
                <outputDirectory>${jacoco.report.path}</outputDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```


## 生成报告

``` bash
mvn clean test -Dmaven.test.skip=false
```

生成 其他目录下面
``` bash
mvn clean test -Dmaven.test.skip=false -Djacoco.report.path=/opt/websuite/nginx/html
```

# 接口测试覆盖率

通过在JVM启动参数中加入`-javaagent`参数指定 JaCoCo 的代理程序，在Class Loader装载一个class前将统计代码插入class文件，达到在执行测试代码或者人工功能测试的时候，实时统计覆盖率的目的。



## 下载 Agent

下载地址： <https://github.com/jacoco/jacoco/releases>，下载之后解压，找到 lib/jacocoagent.jar 文件

## 配置 Agent

```bash
-javaagent:/opt/jacocoagent.jar=includes=xyz.kail.*,output=tcpserver,address=127.0.0.1,port=8110
```

1. javaagent ： jacocoagent.jar 文件的的全路径
2. includes： 为需要分析的 包
3. output： 输出覆盖率报告数据的方式，其它还有 
4. address ： 本机IP
5. port： 暴露的端口

详见官方文档： <https://www.eclemma.org/jacoco/trunk/doc/agent.html>

## dump报告数据

找到 lib/jacococli.jar

``` bash
# dump 数据到 /opt/jacoco.exec 文件
java -jar jacococli.jar dump --address 127.0.0.1 --port 8110 --destfile /opt/jacoco.exec
```

## 生成 html 覆盖率报告

``` bash
java -jar jacococli.jar \
# 指定报告数据文件的路径
report /opt/jacoco.exec \
# 指定项目编译后的 class 文件路径
--classfiles /workspace/some-project/target/some-project/WEB-INF/classes \
# 指定生成 HTML 报告路径
--html /opt \
#指定源码路径（如果不指定无只能看到类和方法的覆盖率，没办法看到具体业务逻辑的服务概率）
--sourcefiles /workspace/some-project/src/main/java \
# 指定编码方式
--encoding utf-8
# 指定报告名称
--name some-project
```

## 其他

1. JVM 启动 java agent 参数中加上 jmx 配置，可通过 MBean 的方式操作 dump 数据
2. 官方有提供 可通过代码导出生成报告的示例，可封装成 HTTP 接口 进行管理

# 与其他工具对比

| 原理         | 使用 ASM 修改字节码                   | 修改 jar 文件，class 文件字节码文件                          | 基于 jcoverage,基于 asm 框架对 class 文件插桩                |
| ------------ | ------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 覆盖粒度     | 行，类，方法，指令，分支              | 行，类，方法，基本块，指令，无分支覆盖                       | 项目，包，类，方法的语句覆盖/分支覆盖                        |
| 插桩         | on the fly、offline                   | on the fly、offline                                          | offline，把统计代码插入编译好的class文件中                   |
| 生成结果     | exec 数据文件                         | html、xml、txt，二进制格式报表                               | html，xml                                                    |
| 缺点         | 需要源代码                            | 1、需要 debug 版本，并打来 build.xml 中的 debug 编译项；<br />2、需要源代码，且必须与插桩的代码完全一致 | 1、不能捕获测试用例中未考虑的异常；<br />2、关闭服务器才能输出覆盖率信息 |
| 性能         | 快                                    | 小巧                                                         | 插入的字节码信息更多                                         |
| 执行方式     | maven，ant，命令行Sonar、Jenkins、IDE | 命令行                                                       | maven，ant                                                   |
| Jenkins 集成 | 生成 html 报告，展示报告，无趋势图    | 无法与 hudson 集成                                           | 有集成的插件，美观的报告，有趋势图                           |
| 报告实时性   | 默认关闭，可以动态从 jvm dump 出数据  | 可以不关闭服务器                                             | 默认是在关闭服务器时才写结果                                 |
| 维护状态     | 持续更新中                            | 停止维护                                                     | 停止维护                                                     |

> 以上对比结果来自： [Jacoco Code Coverage](https://www.jianshu.com/p/16a8ce689d60)

# Read More

- [jacoco 官网](https://www.eclemma.org/jacoco/trunk/)
- [jacoco 官方文档](https://www.eclemma.org/jacoco/trunk/doc/index.html)
- [Jenkins JaCoCo Plugin](https://wiki.jenkins.io/display/JENKINS/JaCoCo+Plugin)
- 
- 腾讯 TMQ [JAVA 代码覆盖率工具 JaCoCo-原理篇](https://mp.weixin.qq.com/s/kdUjmiHerSw365qA66ZKiw)
- 腾讯 TMQ [JAVA 代码覆盖率工具 JaCoCo-实践篇](https://mp.weixin.qq.com/s/lQDA4JmGkNEWqCfGbvmyFg)
- 腾讯 TMQ [JAVA 代码覆盖率工具 JaCoCo-采坑篇](https://mp.weixin.qq.com/s/DKb1N-udAfiBc6R9Wy0lTQ)