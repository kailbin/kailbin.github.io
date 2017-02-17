---
title: Java 方法执行超时时间
date: 2016-12-20 00:00:00
desc: Java 方法执行超时时间,concurrent,ExecutorService,Future,Executors

tags: [JDK5,Java]

---


##### 执行时间超时

```java
  
import java.util.concurrent.*;
  
public class MethodTimeOutTest {  
  
    public static void main(String[] args) throws ExecutionException, InterruptedException {
  
        ExecutorService executorService = Executors.newSingleThreadExecutor();
  
        Future<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                System.out.println("开始执行");
                TimeUnit.SECONDS.sleep(2);
                return "执行完成";
            }
        });
  
        try {
            String result = future.get(1, TimeUnit.SECONDS); // 阻塞直到执行完成，设置超时间
            System.out.println(result);
        } catch (TimeoutException e) {
            System.out.println("超时了");
        } finally {
            executorService.shutdown();
            System.out.println("over !!!!! ");
        }
  
    }
}  
```
**输出**

    开始执行
    超时了
    over !!!!! 

##### 在超时时间内完成执行

超时时间设置为3秒
```java
    ……
    String result = future.get(3, TimeUnit.SECONDS); // 阻塞直到执行完成，设置超时间
    ……
```

**输出**

    开始执行
    执行完成
    over !!!!! 

    
