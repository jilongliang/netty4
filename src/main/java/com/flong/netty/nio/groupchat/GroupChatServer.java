package com.flong.netty.nio.groupchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * @Author:liangjl
 * @Date: 2020/7/4-12:30
 * @Eamil:jilongliang@sina.com
 * @Description:
 */
public class GroupChatServer {

    private Selector selector;
    private ServerSocketChannel listenServerSocketChannel;
    private static final int PORT = 6667;

    public GroupChatServer() {
        try {
            //打开Selector
            selector = Selector.open();
            //打开ServerSocketChannel
            listenServerSocketChannel = ServerSocketChannel.open();
            //绑定端口
            listenServerSocketChannel.socket().bind(new InetSocketAddress(PORT));
            //设置非阻塞模式
            listenServerSocketChannel.configureBlocking(false);

            listenServerSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    /**
     * 监听
     */
    void listen() {

        try {
            while (true) {

                //阻塞2秒钟
                int count = selector.select(2000);
                //大与0代表有事件处理
                if (count > 0) {
                    //遍历得到的SelectionKey
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                        while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        //监听到accept
                        if (key.isAcceptable()) {

                            SocketChannel socketChannel = listenServerSocketChannel.accept();
                            socketChannel.configureBlocking(false);

                            //将socketChannel注册到selector,读
                            socketChannel.register(selector, SelectionKey.OP_READ);

                            //提示某某上线了
                            System.out.println(socketChannel.getRemoteAddress() + "上线了");

                        }

                        //可读的状态，即通道是可读的状态
                        if (key.isReadable()) {
                            //处理读
                            readData(key);
                        }
                        //当前的key删除，防止重复处理
                        iterator.remove();
                    }

                } else {
                    System.out.println("等待中");
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {

        }
    }

    /**
     * 读取客户端消息数据
     */
    private void readData(SelectionKey selectionKey) {
        //定义一个SocketChannel
        SocketChannel socketChannel = null;
        try {
            socketChannel = (SocketChannel) selectionKey.channel();

            //创建Buffer
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            int count = socketChannel.read(buffer);

            if (count > 0) {
                //把缓冲区数据转成字符串
                String msg = new String(buffer.array());
                //输出信息
                System.out.println("来自客户端的消息：" + msg);
                //向其他客户端转发消息，专门写一个方法进行处理

                try {
                    sendInfoToOtherClient(msg, socketChannel);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        } catch (IOException exception) {
            try {
                System.out.println(socketChannel.getRemoteAddress() + "离线");

                //取消注册
                selectionKey.cancel();
                //关闭通道
                socketChannel.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 向其他客户端转发消息，专门写一个方法进行处理
     */
    private void sendInfoToOtherClient(String msg, SocketChannel self) throws Exception {
        System.out.println("服务器转发消息中..");

        //遍历所有注册到Selector上，SocketChannel并排除它自己
        for (SelectionKey key : selector.keys()) {
            Channel targetChannel = key.channel();

            //排除自己
            if (targetChannel instanceof SocketChannel && targetChannel != self) {
                SocketChannel dest = (SocketChannel) targetChannel;
                //将msg存储到buffer
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                //将buffer的数据写入通道
                dest.write(buffer);
            }
        }


    }


    public static void main(String[] args) {
        //创建一个服务对象
        GroupChatServer groupChatServer = new GroupChatServer();
        groupChatServer.listen();

    }
}
