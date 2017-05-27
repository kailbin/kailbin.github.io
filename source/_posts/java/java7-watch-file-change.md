---
title: JDK7 WatchService 监控文件变化
date: 2017-2-18
desc: java,WatchService,监控目录树,监控文件变化

tags: [WatchService,jcmd,NIO2,JDK7]
categories: Java
---

JDK7提供了一系列的API用来监控文件目录的变化，该API提供了一种监视机制，可以针对事件（如创建、修改和删除）监视特定文件或目录的状态。

> 但是该API并**不保证监视事件是采用推送模型**，大部分情况下会使用轮询机制。监视服务也依赖于系统，所以无法利用这种服务构建真正可移植的应用。

因为 WatchService 本身并不支持递归 Watch，Watch一个文件夹只能Watch一层，子文件夹里面变动并不能Watch到，所有这里使用 [FileVisitor](/post/2017-01-07-java7-walkFileTree-fileVisitor.html) 遍历所有目录注册 Watch。

文本在Mac下测试，如果监控`/`根目录的话(包含可访问的所有子目录)，遍历完成大概花了5分钟左右，因为是采用轮训的方式，监控效率并不高，创建一个文件半天看不到效果。

<!--more-->

而且资源消耗也不少，通过`jcmd <pid> GC.heap_dump <filepath>`拉出堆栈信息，`sun.nio.fs.PollingWatchService$PollingWatchKey`该类就占用了300多兆的空间，
`sun.nio.fs.UnixPath`和`sun.nio.fs.PollingWatchService$CacheEntry`有100多万个实例，占了大部分的空间，所有还是不建议监控过多的文件。




# 直接上代码，拷贝运行，查看效果

在当前目录下，创建文件夹、创建文件、修改文件、删除文件，查看输出。

``` java

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;

public class WatchDirectoryTree {

    // 遍历目录树
    private void registerTree(final WatchService watchService, final Path start) throws IOException {
        SimpleFileVisitor<Path> simpleFileVisitor = new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

                if (exc != null) {
                    printLog(dir, exc);
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {

                printLog(file, exc);

                return FileVisitResult.CONTINUE;
            }
        };

        Files.walkFileTree(start, simpleFileVisitor);

    }

    // 监控文件夹和子文件夹
    public void watch(Path start) throws IOException, InterruptedException {

        WatchService watchService = FileSystems.getDefault().newWatchService();

        long startTime = System.currentTimeMillis();
        registerTree(watchService, start); // 遍历所有目录注册到watchService
        printLog("用时" + (System.currentTimeMillis() - startTime), null);


        while (true) {

            final WatchKey key = watchService.take(); // 如果没有会阻塞等待 (LinkedBlockingDeque)

            for (WatchEvent<?> watchEvent : key.pollEvents()) {

                final Kind<?> kind = watchEvent.kind();

                final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
                final Path directory = (Path) key.watchable();
                final Path filename = watchEventPath.context();


                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                // 如果创建事件，并且是创建目录的话，监控该目录的变化
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    final Path child = directory.resolve(filename);

                    if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                        registerTree(watchService, child);
                    }
                }

                printLog(kind + " ---> " + directory.toAbsolutePath() + "/" + filename, null);
            }

            //
            key.reset();

        }
    }

    private static void printLog(Object obj, Throwable ex) {

        if (null != ex) {
            System.out.print("异常信息：");
        }

        System.out.print(obj);

        if (null != ex) {
            System.out.print("，原因：" + ex.getClass() + ": " + ex.getMessage());
        }
        System.out.println();

    }

    public static void main(String[] args) throws IOException, InterruptedException {

        final Path path = Paths.get("./"); // 监控的目录为当前运行的目录

        new WatchDirectoryTree().watch(path);

    }
}
```

# 参考
> [详解Java 7中新的文件API](http://www.infoq.com/cn/articles/java7-nio2/)
>
> [监控服务 API](http://cucaracha.iteye.com/blog/2051279)、[实现文件监控服务](http://cucaracha.iteye.com/blog/2055653)、[监控目录树](http://cucaracha.iteye.com/blog/2057050)

