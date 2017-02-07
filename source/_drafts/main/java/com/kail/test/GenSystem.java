package com.kail.test;

public class GenSystem {

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            // System.currentTimeMillis(); // Integer.MAX_VALUE -> 10573 (20w/每毫秒)
            // System.nanoTime(); // Integer.MAX_VALUE -> 29226        (7w/每毫秒)
        }
        System.out.println(System.currentTimeMillis() - time);
    }

}