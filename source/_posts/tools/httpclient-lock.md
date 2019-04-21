---
title: 记一次 HttpClient 死锁问题
tags:
  - Java
categories:
  - Tools
date: 2019-04-21
id: tools/httpclient-lock
---



最近遇到一个使用 Apache HttpClient 过程中的问题，具体场景是

- 通过 Spring `@Scheduled(cron = "..")` 方式执行定时任务
- 定时任务中并发使用 HttpClient 拉取数据
- 但是定时任务只会执行一次
- 因为 Spring 基于注解的定时任务，在非异步的情况的，上一次任务执行完之前不会执行下一个任务
- 所以怀疑是第一次执行的任务一直没有执行完，卡在了某个地方



<!-- more -->



## 还原场景

maven 依赖

```html
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpcore</artifactId>
    <version>4.4.6</version>
</dependency>

<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.3</version>
</dependency>
```



程序简化后，代码如下

```java
package xyz.kail.demo.java.se.temp;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpClientMain {


    public static void main(String[] args) throws IOException, InterruptedException {

        int count = 20;

        CountDownLatch countDownLatch = new CountDownLatch(count);

        CloseableHttpClient httpClient = HttpClients.createDefault();

        ExecutorService executorService = Executors.newFixedThreadPool(count);

        for (int i = 0; i < count; i++) {

            executorService.submit(() -> {
                countDownLatch.countDown(); // line num: 32
                try {
                    request(httpClient);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });

        }

        countDownLatch.await();  // line num: 42
        System.out.println("countDownLatch.await();");

        httpClient.close();  // line num: 45
        System.out.println("httpClient.close();");

        executorService.shutdown();
        System.out.println("executorService.shutdown();");


    }

    private static void request(CloseableHttpClient client) throws IOException, InterruptedException {

        HttpGet request = new HttpGet("http://blog.kail.xyz");
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(1_000)
                .setSocketTimeout(5_000)
                .build();

        request.setConfig(requestConfig);
        try (CloseableHttpResponse response = client.execute(request)) { // line num: 63
            String data = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        } finally {
            request.releaseConnection();
        }
    }
}

```

正常情况下，以上程序会输出

> countDownLatch.await();
> httpClient.close();
> executorService.shutdown();

但是多运行几次，会发现有时候会只输出 `countDownLatch.await();`，程序会卡在 `httpClient.close();`



## 查看线程信息

```bash
$ jcmd
...
51051 xyz.kail.demo.java.se.temp.HttpClientMain

$ jcmd 51051 Thread.print
...

"pool-1-thread-20" #30 prio=5 os_prio=31 tid=0x00007fbb5b22c000 nid=0x6803 waiting on condition [0x0000700005997000]
   java.lang.Thread.State: WAITING (parking) # ❤❤❤❤ 关注 WAITING
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x000000076c0e7488> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
	# ❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤ 关注
	at org.apache.http.pool.AbstractConnPool.getPoolEntryBlocking(AbstractConnPool.java:377)
	at org.apache.http.pool.AbstractConnPool.access$200(AbstractConnPool.java:67)
	at org.apache.http.pool.AbstractConnPool$2.get(AbstractConnPool.java:243)
	- locked <0x000000076de511b8> (a org.apache.http.pool.AbstractConnPool$2)
	at org.apache.http.pool.AbstractConnPool$2.get(AbstractConnPool.java:191)
	at org.apache.http.impl.conn.PoolingHttpClientConnectionManager.leaseConnection(PoolingHttpClientConnectionManager.java:282)
	at org.apache.http.impl.conn.PoolingHttpClientConnectionManager$1.get(PoolingHttpClientConnectionManager.java:269)
	at org.apache.http.impl.execchain.MainClientExec.execute(MainClientExec.java:191)
	at org.apache.http.impl.execchain.ProtocolExec.execute(ProtocolExec.java:185)
	at org.apache.http.impl.execchain.RetryExec.execute(RetryExec.java:89)
	at org.apache.http.impl.execchain.RedirectExec.execute(RedirectExec.java:111)
	at org.apache.http.impl.client.InternalHttpClient.doExecute(InternalHttpClient.java:185)
	at org.apache.http.impl.client.CloseableHttpClient.execute(CloseableHttpClient.java:83)
	at org.apache.http.impl.client.CloseableHttpClient.execute(CloseableHttpClient.java:108)
	# ❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤ 关注
	at xyz.kail.demo.java.se.temp.HttpClientMain.request(HttpClientMain.java:63)
	at xyz.kail.demo.java.se.temp.HttpClientMain.lambda$main$0(HttpClientMain.java:34)
	at xyz.kail.demo.java.se.temp.HttpClientMain$$Lambda$2/2137589296.run(Unknown Source)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)

...

"main" #1 prio=5 os_prio=31 tid=0x00007fbb5a802000 nid=0x1903 waiting for monitor entry [0x0000700003732000]
   java.lang.Thread.State: BLOCKED (on object monitor) # ❤❤❤❤ 关注 BLOCKED
	# ❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤ 关注
	at org.apache.http.pool.AbstractConnPool$2.cancel(AbstractConnPool.java:207)
	- waiting to lock <0x000000076c88eb20> (a org.apache.http.pool.AbstractConnPool$2)
	at org.apache.http.pool.RouteSpecificPool.shutdown(RouteSpecificPool.java:155)
	at org.apache.http.pool.AbstractConnPool.shutdown(AbstractConnPool.java:152)
	at org.apache.http.impl.conn.PoolingHttpClientConnectionManager.shutdown(PoolingHttpClientConnectionManager.java:396)
	at org.apache.http.impl.client.HttpClientBuilder$2.close(HttpClientBuilder.java:1225)
	at org.apache.http.impl.client.InternalHttpClient.close(InternalHttpClient.java:201)
	# ❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤ 关注
	at xyz.kail.demo.java.se.temp.HttpClientMain.main(HttpClientMain.java:45)

...
```

## 回忆一下线程状态

```java
package java.lang;

...

public class Thread implements Runnable {

 ...

   public enum State {
        /**
         * 新创建了一个线程对象，但还没有调用start()方法
         */
        NEW,

        /**
         * Thead.start
         */
        RUNNABLE,

        /**
         * 等待/阻塞 获取 synchronized 锁
         */
        BLOCKED,

        /**
         * Object.wait / Thread.join / LockSupport.park 未设置超时时间
         */
        WAITING,

        /**
         * 以下的几种情况
         *   <li>{@link #sleep Thread.sleep}</li>
         *   <li>{@link Object#wait(long) Object.wait} with timeout</li>
         *   <li>{@link #join(long) Thread.join} with timeout</li>
         *   <li>{@link LockSupport#parkNanos LockSupport.parkNanos}</li>
         *   <li>{@link LockSupport#parkUntil LockSupport.parkUntil}</li>
         */
        TIMED_WAITING,

        /**
         * 线程已经执行完成
         */
        TERMINATED;
    }
}
```

> [Java线程的6种状态及切换](https://blog.csdn.net/pange1991/article/details/53860651)

## 根据 Thread.print 信息找到源码位置

### AbstractConnPool.java:377

```java
// 入口
// xyz.kail.demo.java.se.temp.HttpClientMain.request(HttpClientMain.java:63)
// at org.apache.http.impl.client.CloseableHttpClient.execute(CloseableHttpClient.java:108)
private E getPoolEntryBlocking(...){
        ...
        this.lock.lock();
        try {
            ...
                    } else {
...                     // 5️⃣ 线程2 wait，但是这时候线程1已经 BLOCKED
                        this.condition.await(); // line num: 377 [WAITING (parking)]
                        success = true;
                    }
            ...
        } finally {
            this.lock.unlock();
        }
    }
}
```



### AbstractConnPool.java:207

```java
@Override
public Future<E> lease(final T route, final Object state, final FutureCallback<E> callback) {
   
...

    return new Future<E>() {

...
        // 入口
        // at xyz.kail.demo.java.se.temp.HttpClientMain.main(HttpClientMain.java:45)
        // at org.apache.http.impl.client.InternalHttpClient.close(InternalHttpClient.java:201)
        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            cancelled = true;
            lock.lock();
            try {
                condition.signalAll(); // 1️⃣ 线程1
            } finally {
                lock.unlock();
            }
            // 3️⃣ 线程1 这时候线程2已经获取到锁，这里 BLOCKED
            synchronized (this) { // line num 207 BLOCKED (on object monitor)
...
            }
        }

...

        @Override
        public E get(final long timeout, final TimeUnit tunit) throws InterruptedException, ExecutionException, TimeoutException {
...
            synchronized (this) { // 2️⃣ 线程2 获取锁
...                     // 4️⃣ 线程2 执行 getPoolEntryBlocking 方法
                        final E leasedEntry = getPoolEntryBlocking(...); // 调用 377 行的代码
...
            }
        }

    };
}
```

### 可能原因分析

根据调用入口 大致可以确定 是 **close 释放 HttpClient 资源的时候** 和 **execute 请求获取资源的时候** 产生了死锁。

模拟可能的执行流程如下：

- 线程1：condition.signalAll() 
- 线程2： 获取 this 锁
- **线程1：获取 this 锁 失败，BLOCKED**
- 线程2：执行 getPoolEntryBlocking 方法
  - **线程2：condition.wait (WAITING (parking))**
- 最终两个线程 在互相等待对方释放锁/唤醒，产生死锁



分析到这基本上可以确定 应该是 httpcore 中 `org.apache.http.pool.AbstractConnPool` 这个类的Bug

## 如何解决

如果是 HttpClient (httpcore 模块) 的 Bug，可以看一下官方有没有修复，到 Github 官方仓库 httpcomponents-core 找到指定的文件 [org/apache/http/pool/AbstractConnPool.java](https://github.com/apache/httpcomponents-core/blob/4.4.x/httpcore/src/main/java/org/apache/http/pool/AbstractConnPool.java) 查看 [提交历史](https://github.com/apache/httpcomponents-core/commits/4.4.x/httpcore/src/main/java/org/apache/http/pool/AbstractConnPool.java)， Ctrl + F 搜索 关键字 `fix`，最终找到了这次提交 [HTTPCORE-446: fixed deadlock in AbstractConnPool shutdown code](https://github.com/apache/httpcomponents-core/commit/92de9e01ad59e1ddeaa250623bb8e42129036c45#diff-9db2ef2f1b94de8de5d1df36b2e44a03)



点击这次提交 右侧的 `<>` 按钮(Browse the repository at this point in the history) 查看这次提交后的 git 仓库，发现修复之后 httpcore 的版本是 `4.4.7-SNAPSHOT` 。



升级到 httpcore maven 版本到 `4.4.7+` 后重试最初的代码，发现死锁问题已经解决，但是会抛出以下异常：

```bash
org.apache.http.impl.execchain.RequestAbortedException: Request aborted
	at org.apache.http.impl.execchain.MainClientExec.execute(MainClientExec.java:194)
	at org.apache.http.impl.execchain.ProtocolExec.execute(ProtocolExec.java:185)
	at org.apache.http.impl.execchain.RetryExec.execute(RetryExec.java:89)
	at org.apache.http.impl.execchain.RedirectExec.execute(RedirectExec.java:111)
	at org.apache.http.impl.client.InternalHttpClient.doExecute(InternalHttpClient.java:185)
	at org.apache.http.impl.client.CloseableHttpClient.execute(CloseableHttpClient.java:83)
	at org.apache.http.impl.client.CloseableHttpClient.execute(CloseableHttpClient.java:108)
...
Caused by: java.lang.InterruptedException: Operation interrupted
	at org.apache.http.pool.AbstractConnPool.getPoolEntryBlocking(AbstractConnPool.java:384)
...
```



## 如何避免

- 多线程并发使用共享资源的时候，如果不了解共享资源的内部机制，不了解是否存在并发问题的时候，一定要小心，如果不分析源码，最好也上网查一下相关的问题，如："httpclient 并发问题" 等
- CountDownLatch 的使用方式也存在问题，比如这个示例程序中，countDownLatch.countDown() 写在了线程执行逻辑的第一行，真正的逻辑还没开始执行，就已经 countDown，实际上并没有起到相应的作用
- 如果确定共享资源存在并发问题，并且不确定官方有没有提供相应的解决方案的话，最快但不是最好的方式是：把共享资源放到线程内作为线程内部的资源，避免并发问题
- ...



## 其它收获

- http-core 与 httpclient/httpmime 是分开两个仓库维护的，所以 maven 版本号不一定一致，但是 httpclient/httpmime 是同一个仓库下的两个模块，理论上版本号应该是一致的
- [使用httpclient必须知道的参数设置及代码写法、存在的风险](https://jinnianshilongnian.iteye.com/blog/2089792)
  - 必须设置超时时间，否则可能在 网络 IO 上 卡死
  - 默认重试3次机制
  - 连接池管理
- [关于HttpClient重试策略的研究](https://www.cnblogs.com/kingszelda/p/8886403.html)
- [Apache HttpClient 资源释放、请求超时，导致线程池用光、内存不足](https://blog.csdn.net/ClementAD/article/details/75649625)
- [HttpClient多线程并发问题](https://blog.csdn.net/kobejayandy/article/details/16921265)



