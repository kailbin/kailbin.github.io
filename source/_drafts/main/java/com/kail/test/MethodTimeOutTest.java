package com.kail.test;

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
            String result = future.get(3, TimeUnit.SECONDS); // 阻塞，设置超时间
            System.out.println(result);
        } catch (TimeoutException e) {
            System.out.println("超时了");
        } finally {
            executorService.shutdown();
            System.out.println("over !!!!! ");
        }

    }
}  