---
title: JDK7 遍历文件的API
date: 2017-01-07 00:00:00
desc:  JDK7 遍历文件的API
---
TODO

<!--more-->

``` java
/**
 * Created by kail on 2017/1/8.
 */
public class Nio2Main {

    public static void main(String[] args) throws IOException {

        Files.walkFileTree(Paths.get("."), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println(dir.toFile().getAbsoluteFile() + "\\");
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println(file.toFile().getAbsoluteFile());
                return super.visitFile(file, attrs);
            }
        });
    }


}

```

### 参考

> [[疯狂Java]NIO.2：walkFileTree、FileVisitor（遍历文件/目录）](http://blog.csdn.net/lirx_tech/article/details/51424569)
>
>[iteye - 博客专栏 - Java NIO.2](http://www.iteye.com/blogs/subjects/java-nio-2)