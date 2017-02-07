package com.kail.test;

import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Kail on 2017/1/19.
 * Copyright(c) ttpai All Rights Reserved.
 */
public class Main {

    static List<Integer> aa = new CopyOnWriteArrayList<>();

    public static void run() {
        List ex = aa;
        if (ex.size() > 0) {
            ArrayList tmpTaskList = new ArrayList();
            synchronized (ex) {
                System.out.println("sleet");

                Iterator var4 = ex.iterator();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (true) {
                    if (!var4.hasNext()) {
                        break;
                    }
                    Integer taskItemDefine = (Integer) var4.next();
                    tmpTaskList.add(taskItemDefine);
                }
            }
        }
    }


    public static void main(String[] args) {
//        System.out.println((int) (Math.random() * 10) % 2);
//        System.out.println((int) (Math.random() * 10) % 2);
//        System.out.println((int) (Math.random() * 10) % 2);
//        System.out.println((int) (Math.random() * 10) % 2);
//        System.exit(1);

        for (int i = 0; i < 1000; i++) {
            final int j = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if ((int) (Math.random() * 10) % 2 > 0) {
                        aa.add(1);
                        aa.add(1);
                    } else {
                        try {
                            aa.remove(1);
                        } catch (ArrayIndexOutOfBoundsException e) {

                        }
                    }
                    System.out.println("--" + j);
                }
            }).start();
        }

        for (int i = 0; i < 1000; i++) {
            final int j = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Main.run();
                    System.out.println("==" + j);

                }
            }).start();
        }


    }


}
