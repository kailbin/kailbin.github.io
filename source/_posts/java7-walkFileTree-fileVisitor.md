---
title: JDK7 遍历文件的API
date: 2017-01-07 00:00:00
desc:  JDK7 遍历文件的API

tags: [JDK7,Java,NIO2]

---

JDK7 遍历文件系统，需要通过递归的方式，JDK7+ 直接提供了遍历文件的API。
``` java 
public static Path walkFileTree(Path start, FileVisitor<? super Path> visitor){}

public static Path walkFileTree(Path start, Set<FileVisitOption> options, int maxDepth, FileVisitor<? super Path> visitor){}
```

<!--more-->

### 相关类与参数介绍

#### FileVisitor<? super Path>

``` java
package java.nio.file;
  
public interface FileVisitor<T> {
  
    /**
     * 访问一个路径之前
     */
    FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs) throws IOException;
      
    /**
     * 访问一个路径之后
     */
    FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException;
  
    /**
     * 访问文件时候
     */
    FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException;
  
    /**
     * 访问文件失败的时候
     */
    FileVisitResult visitFileFailed(T file, IOException exc) throws IOException;

}
```
使用的时候一般不直接使用 `FileVisitor`，而是使用它的子类 `SimpleFileVisitor`，这样可以只关注其中某几个事件而不用全部事件。

#### BasicFileAttributes

包含 `创建时间`、`修改时间` 等一些文件属性，详见：

> http://docs.oracle.com/javase/7/docs/api/java/nio/file/attribute/BasicFileAttributes.html

#### FileVisitResult

``` java

package java.nio.file;
  
public enum FileVisitResult {
  
    /**
     * 继续遍历，只针对 FileVisitor.preVisitDirectory 和 FileVisitor.preVisitDirectory 方法 
     */
    CONTINUE,  
      
    /**
     * 停止遍历
     */
    TERMINATE,  
  
    /**
     * 继续遍历，但是忽略子目录，但是子文件还是会访问；
     */
    SKIP_SUBTREE,  
  
    /**
     * 继续遍历，但忽略当前节点的所有兄弟节点直接返回上一层继续遍历
     */
    SKIP_SIBLINGS;
}

```

### 文件过滤

#### Path

``` java
boolean startsWith(...); // 前缀检查
boolean endsWith(...); // 后缀检查
```

#### PathMatcher
 
获取方法
 
 ``` java
PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(String syntaxAndPattern);
 ```
 
参数`syntaxAndPattern` 支持 `glob` 和 `regex` 两种语法，格式是 `syntax:pattern` 。
 
glob 的 语法如下：
 
- `*` 匹配零个或多个字符，但是不跨目录匹配
- `**` 匹配零个或多个字符，跨目录匹配
- `?` 只匹配一个字符
- `\` 是转义字符，`\\`匹配一个反斜杠， `\{` 匹配一个左大括号
- `[ ]` 语法类似于正则表达式，匹配中括号内的所有字符。`[abc]` 匹配 a 或 b 或 c ; `[a-z]` 匹配26个字母;  `[abe-g]`匹配a、b、e、f、g; `[!a-c]` 匹配除了a、b、c 外的所有字符。 中括号内的 `*` `?` `\` 没有特殊含义，`-`放在第一位也没有特殊含义
- `{ }` 是一个用 `,` 分割的组，组不能嵌套。
  
详见：
> <a href="http://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)">http://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)</a>

`regex` 的语法详见：
> http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html

### 例子

#### 打印当前目录下面的所有以`.class`结尾的文件

``` java
final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**.class");

Files.walkFileTree(Paths.get("."), new SimpleFileVisitor<Path>() {
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (pathMatcher.matches(file)) {
            System.out.println(file);
        }
        return super.visitFile(file, attrs);
    }
});
```

### 参考

> [详解Java 7中新的文件API](http://www.infoq.com/cn/articles/java7-nio2/)
>
> [[疯狂Java]NIO.2：walkFileTree、FileVisitor（遍历文件/目录）](http://blog.csdn.net/lirx_tech/article/details/51424569)
>
>[iteye - 博客专栏 - Java NIO.2](http://www.iteye.com/blogs/subjects/java-nio-2)