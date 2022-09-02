package com.sec.local;

import com.sec.Constant;
import com.sec.local.handler.PublicListenerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class Server {

    public static void main(String[] args) {
        server();
    }

    static void server() {
        //一般本地已经启动服务，下边的服务只是响应发送来的数据
//        NioEventLoopGroup group = new NioEventLoopGroup();
//        ChannelFuture future = new ServerBootstrap()
//                .group(group)
//                .channel(NioServerSocketChannel.class)
//                .childHandler(new ChannelInitializer<NioSocketChannel>() {
//                    protected void initChannel(NioSocketChannel ch) throws Exception {
//                        ch.pipeline()
//                                .addLast(new ChannelInboundHandlerAdapter() {
//                                    @Override
//                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                        //只是负责转发，收到什么消息发送什么
//                                        ch.writeAndFlush(msg);
//                                    }
//                                });
//                    }
//                })
//                .bind(Constant.LOCAL_PORT);
//        try {
//            Channel channel = future.sync().channel();
//            channel.closeFuture().addListener((ch) -> {
//                group.shutdownGracefully();
//            });
//        } catch (
//                InterruptedException e) {
//            e.printStackTrace();
//            group.shutdownGracefully();
//        }

        //启动连接远程的客户端
        for (int i = 0; i < 18; i++) {
            remote();
        }
    }

    static void remote() {
        NioEventLoopGroup g = new NioEventLoopGroup();
        ChannelFuture channel = new Bootstrap().group(g)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(1 << 20, 0, 4, 4, 4))
                                .addLast(new PublicListenerHandler());
                    }
                }).connect(Constant.PUBLIC_IP, Constant.LISTEN_IN);

        try {
            channel.channel().closeFuture().addListener((c) -> g.shutdownGracefully());
        } catch (Exception e) {
            g.shutdownGracefully();
        }
    }

}
