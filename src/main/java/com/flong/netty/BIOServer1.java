package com.flong.netty;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @Description BIOServer1
 * @Date 2019/11/20 15:15
 * @Author liangjl
 * @Version V1.0
 * @Copyright (c) All Rights Reserved, 2019.
 */
public class BIOServer1 {

    public static void main(String[] args) throws Exception {

        //jdk的JUC(Java util concurrent )的类
        //ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();

        //ali-Check代码插件建议使用BasicThreadFactory是commons-lang3插件的一个类
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());

        //ServerSocket创建
        ServerSocket serverSocket = new ServerSocket(7001);

        System.out.println("服务器启动");

        while (true) {

            Socket socket = serverSocket.accept();

            //使用jdk1.8新特性的lambda表达式，代替传统的new Runnable()
            executorService.execute(() -> {
                handler(socket);
            });

            System.out.println("连接到一个客户端");
        }

    }


    private static void handler(Socket socket) {

        try {
            byte[] bytes = new byte[1024];

            InputStream inputStream = socket.getInputStream();

            while (true) {

                int read = inputStream.read(bytes);
                if (read != -1) {
                    System.out.println(new String(bytes, 0, read));
                } else {
                    //跳出
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //判断不为空的时候就处理关闭
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
