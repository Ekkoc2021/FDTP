package com.fdtp;

import com.fdtp.dt.tp.DisrutorThreadPoolExecutor;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DisruptorFDTPTest {

    public static String calculateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    @Test
    public void DisruptorCpuBoundTest() {
        DisrutorThreadPoolExecutor disrutorThreadPoolExecutor = new DisrutorThreadPoolExecutor(100);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 40; i++) {
            disrutorThreadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] data = new byte[1024]; // 1KB 数据
                    new Random().nextBytes(data);
                    DisruptorFDTPTest.calculateSha256(data);
                    System.out.println(Thread.currentThread().getName());
                }
            });
        }
        disrutorThreadPoolExecutor.shutdown();
        long end = System.currentTimeMillis();
        System.out.println("disruptor:" + (end - start));
    }

    @Test
    public void CpuBoundTest() {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(100, 100, 4, TimeUnit.SECONDS, new ArrayBlockingQueue<>(524288));

        long start = System.currentTimeMillis();
        for (int i = 0; i < 40; i++) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] data = new byte[1024]; // 1KB 数据
                    new Random().nextBytes(data);
                    DisruptorFDTPTest.calculateSha256(data);
                    System.out.println(Thread.currentThread().getName());
                }
            });
        }


        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                // 3. 超时后强制关闭
                pool.shutdownNow();
                System.out.println("部分任务未完成，强制关闭");
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        long end = System.currentTimeMillis();
        System.out.println("pool:" + (end - start));

    }
}
