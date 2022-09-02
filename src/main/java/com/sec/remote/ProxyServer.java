package com.sec.remote;

import com.sec.Constant;
import com.sec.remote.handler.ClientListenerHandler;
import com.sec.remote.handler.LocalListenerHandler;
import com.sec.remote.handler.LocalReaderHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

//本地服务器：无公网ip，连接的wifi或热点
//远程服务器：有公网ip
public class ProxyServer {


    public static void main(String[] args) {
        //监听请求连接，为每个通道赋上编号
        server1();
        //监听本地连接
        server2();
    }

    static void server1() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture future = new ServerBootstrap()
                .group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ClientListenerHandler());
                    }
                }).bind(Constant.LISTEN_OUT);
        try {
            Channel channel = future.sync().channel();
            channel.closeFuture().addListener((ch) -> {
                group.shutdownGracefully();
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
            group.shutdownGracefully();
        }
    }

    static void server2() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture future = new ServerBootstrap()
                .group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(1 << 20, 0, 4, 4, 4))
                                .addLast(new LocalListenerHandler())
                                .addLast(new LocalReaderHandler());
                    }
                }).bind(Constant.LISTEN_IN);
        try {
            Channel channel = future.sync().channel();
            channel.closeFuture().addListener((ch) -> {
                group.shutdownGracefully();
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
            group.shutdownGracefully();
        }
    }
}

