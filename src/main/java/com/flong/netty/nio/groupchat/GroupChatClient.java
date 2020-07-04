package com.flong.netty.nio.groupchat;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

/**
 * @Author:liangjl
 * @Date: 2020/7/4-13:13
 * @Eamil:jilongliang@sina.com
 * @Description:
 */
public class GroupChatClient {

    //定义一个相关的属性
    private String HOST = "127.0.0.1";
    private int PORT = 6667;

    private Selector selector;
    private SocketChannel socketChannel;
    private String userName;

    public GroupChatClient() {
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            userName = socketChannel.getLocalAddress().toString().substring(1);

            System.out.println("====" + userName + "is ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //向服务器发送消息
    public void sendInfo(String info) {
        info = userName + "说" + info;
        try {
            socketChannel.write(ByteBuffer.wrap(info.getBytes()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //读取从服务器端回复的消息
    public void readInfo() {
        try {

            int cout = selector.select();
            //大于0表示有可用的通道
            if (cout > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    //可读
                    if (key.isReadable()) {
                        //得到相关通道
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        //socketChannel.configureBlocking(false);
                        //创建一个ByteBuffer
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        //读到的数据
                        socketChannel.read(buffer);

                        String msg = new String(buffer.array());
                        System.out.println(msg.trim());
                    }
                }
                //移除当前的selectionKey防止重复操作
                iterator.remove();
            } else {
                //System.out.println("没有可用的通道！");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) throws Exception{
        GroupChatClient groupChatClient = new GroupChatClient();

        //启动一个线程，每三秒钟，从服务器进行读取消息
        new Thread(()->{
            while (true){
                groupChatClient.readInfo();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        //扫描消息
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()){
            String msg = scanner.nextLine();
            groupChatClient.sendInfo(msg);

        }

    }

}
