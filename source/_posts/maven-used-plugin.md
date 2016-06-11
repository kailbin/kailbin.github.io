
---

title: maven 常用插件
date: 2015-08-17 22:53:06
desc: maven 常用插件

---

### maven-clean-plugin

可以在构建的时候清理一些文件。

http://maven.apache.org/plugins/maven-clean-plugin/

<!--more-->

示例：

``` xml
<plugin>
    <artifactId>maven-clean-plugin</artifactId>
    <version>2.6.1</version>
    <configuration>
        <filesets>
            <fileset>
                <!-- 带删除文件所在目录 -->
                <directory>${basedir}</directory>
                <includes>
                    <!-- 带删除的文件列表 -->
                    <include>**/*.tmp</include>
                    <include>**/*.log</include>
                    <!-- 删除文件夹 -->
                    <include>file/</include>
                </includes>
                <!-- 排除的不需要删除的文件 -->
                <excludes>
                    <exclude>**/important.log</exclude>
                    <exclude>**/another-important.log</exclude>
                </excludes>
                <followSymlinks>false</followSymlinks>
            </fileset>
        </filesets>
    </configuration>
</plugin>
```



### maven-compiler-plugin

编译配置

http://maven.apache.org/plugins/maven-compiler-plugin/


示例

``` xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.3</version>
    <configuration>
        <source>1.7</source>
        <target>1.7</target>
        <!-- Default value is: ${project.build.sourceEncoding}. -->
        <encoding>utf-8</encoding>
    </configuration>
</plugin>
```
                

### maven-install-plugin

把项目发布到本地仓库  
http://maven.apache.org/plugins/maven-install-plugin/



### maven-deploy-plugin

把项目发布到远程仓库


http://maven.apache.org/plugins/maven-deploy-plugin/


### maven-resources-plugin

拷贝文件到指定目录，该插件共有三个目标：resources:resources、resources:testResources、resources:copy-resources，前两个目标主要是拷贝class，默认即可，不用配置，主要是第三个目标，用于拷贝配置文件。


http://maven.apache.org/plugins/maven-resources-plugin/

``` xml
<plugin>
    <artifactId>maven-resources-plugin</artifactId>
    <executions>
        <execution>
            <!-- 绑定到生命周期的compile阶段，即执行compile的时候就执行该插件 -->
            <phase>compile</phase>
            <!-- 使用插件的copy-resources目标 -->
            <goals>
                <goal>copy-resources</goal>
            </goals>
            <configuration>
                <encoding>UTF-8</encoding>
                <!--拷贝到构建目录conf文件夹下 -->
                <outputDirectory>${project.build.directory}/conf</outputDirectory>
                <resources>
                    <resource>
                        <!-- 需要拷贝的文件夹 -->
                        <directory>src/main/resources</directory>
                        <!-- 需要拷贝的文件 -->
                        <includes>
                            <include>*.properties</include>
                            <include>*.xml</include>
                        </includes>
                        <!-- 排除不用拷贝的文件 -->
                        <excludes>
                            <exclude>*.txt</exclude>
                        </excludes>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>
```
                

### maven-surefire-plugin

测试插件

http://maven.apache.org/surefire/maven-surefire-plugin/

以下配置在构建的时候会跳过测试。使用 -Dmaven.test.skip=true 参数可以达到同样的效果。

``` xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.18.1</version>
    <configuration>
        <skipTests>true</skipTests>
    </configuration>
</plugin>
```
                

Packaging types/tools

### maven-jar-plugin

生成一个可执行jar包

http://maven.apache.org/plugins/maven-jar-plugin/

``` xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>2.6</version>
    <executions>
        <execution>
            <goals>
                <goal>jar</goal>
            </goals>
            <!-- 绑定到编译阶段 -->
            <phase>compile</phase>
            <configuration>
                <!--排除某些配置文件，放在jar的外面，方面修改，-->
                <!--【测试后发现这种写方法“src/main/resources/*”并不能排除文件，这里排除文件的根目录是classpath，不是项目根目录,】 -->
                <excludes>
                    <exclude>**/*.xml</exclude>
                    <exclude>**/*.properties</exclude>
                    <exclude>**/*.txt</exclude>
                </excludes>
                <archive>
                    <!--自动添加META-INF/MANIFEST.MF -->
                    <manifest>
                        <addClasspath>true</addClasspath>
                        <!-- MANIFEST.MF文件里的classPath添加 lib/ 前缀，该插件并不会拷贝依赖jar包到lib文件夹下 -->
                        <classpathPrefix>lib/</classpathPrefix>
                        <mainClass>${main.class}</mainClass>
                    </manifest>
                    <manifestEntries>
                        <Class-Path>. conf/</Class-Path>
                    </manifestEntries>
                </archive>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### maven-war-plugin

打war包，排除文件

http://maven.apache.org/plugins/maven-war-plugin/

``` xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-war-plugin</artifactId>
    <version>2.6</version>
    <configuration>
        <packagingExcludes>WEB-INF/classes/**/*.txt</packagingExcludes>
    </configuration>
</plugin>
```
                





### maven-dependency-plugin


http://maven.apache.org/plugins/maven-dependency-plugin/


以下配置会把项目依赖的jar包拷贝到 lib 目录下面

``` xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <id>copy</id>
            <phase>package</phase>
            <goals>
                <goal>copy-dependencies</goal>
            </goals>
            <configuration>
                <outputDirectory>
                    <!--拷贝到构建目录的lib文件夹下 -->
                    ${project.build.directory}/lib
                </outputDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```
                





### exec-maven-plugin

运行 mvn exec:exec 执行主类

``` xml
<plugin>
    <!--mvn clean compile;mvn exec:exec -->
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>1.2.1</version>
    <configuration>
        <!-- 执行什么样的命令 -->
        <executable>java</executable>
        <arguments>
            <argument>-Djava.io.tmpdir=logs</argument>
            <!-- 工程的classpath并不需要手动指定，它将由exec自动计算得出 -->
            <argument>-classpath</argument>
            <classpath/>
            <argument>${main.class}</argument>
            <argument>arg1</argument>
            <argument>arg2</argument>
        </arguments>
    </configuration>
</plugin>
```
                




### jetty-maven-plugin

使用Jetty 发布：mvn jetty:run


设置端口号： mvn -Djetty.port=8888 jetty:run


http://www.eclipse.org/jetty/documentation/current/jetty-maven-plugin.html

``` xml
<plugin>
    <groupId>org.eclipse.jetty</groupId>
    <artifactId>jetty-maven-plugin</artifactId>
    <version>9.2.13.v20150730</version>
    <configuration>
        <scanIntervalSeconds>10</scanIntervalSeconds>
    </configuration>
</plugin>
```
                


<hr>




## FAQ：

### 如何执行源码中的一个main 方法？

**使用到的插件**：exec-maven-plugin

**配置主方法**：main.class

**执行**：mvn exec:exec


### 如何打一个可执行Jar包，并把配置、依赖项单独放置？

**使用到的插件**：maven-resources-plugin、maven-dependency-plugin、maven-jar-plugin

1. maven-resources-plugin：拷贝配置文件到指定文件夹，例如：conf/

2. maven-dependency-plugin：拷贝依赖项到指定文件夹，例如：lib/

3. maven-jar-plugin：设置主方法和classpath

具体配置请看上面的插件介绍和示例，根据情况具体修改。




### 如何使用Jetty插件，启动web应用？  

**使用到的插件**：jetty-maven-plugin  

**启动方式**：mvn jetty:run


