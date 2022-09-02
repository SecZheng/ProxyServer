package com.sec.local.handler;

import com.sec.Constant;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.StandardCharsets;

public class PublicListenerHandler extends ChannelInboundHandlerAdapter {

    int channelKey;
    Channel remote;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //读取channelKey
        channelKey = ((ByteBuf) msg).readInt();
        remote = ctx.channel();
        Channel connect = connect();
        connect.writeAndFlush(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String key = "login sec hello-sec";
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(key.length());
        buf.writeInt(key.length());
        buf.writeInt(-1);
        buf.writeBytes(key.getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(buf);
    }


    //连接本地服务器
    private Channel connect() {
        NioEventLoopGroup g = new NioEventLoopGroup();
        ChannelFuture channel = new Bootstrap().group(g)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        //本地服务器发送来响应，转发给远程服务器
                                        ByteBuf buf = ctx.alloc().buffer(4);
                                        buf.writeInt(((ByteBuf) msg).readableBytes());
                                        buf.writeInt(channelKey);
                                        remote.writeAndFlush(buf);
                                        remote.writeAndFlush(msg);
                                    }
                                });
                    }
                }).connect(Constant.LOCALHOST, Constant.LOCAL_PORT);

        Channel ch = null;
        try {
            ch = channel.sync().channel();
            ch.closeFuture().addListener((c) -> g.shutdownGracefully());
        } catch (
                Exception e) {
            g.shutdownGracefully();
        }
        return ch;
    }
}
