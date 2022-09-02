package com.sec.remote.handler;

import com.sec.remote.util.ChannelUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

public class LocalListenerHandler extends ChannelInboundHandlerAdapter {
    private static String[] password = {"sec", "hello-sec"};

    Channel remote;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //读取的channelKey为-1代表本地连接
        if (((ByteBuf) msg).getInt(0) == -1) {
            ((ByteBuf) msg).getInt(0);
            String[] s = ((ByteBuf) msg).toString(StandardCharsets.UTF_8).split(" +");
            //需要发送口令才能发送消息
            //口令：login sec hello-sec
            if (s.length != 3 || !(s[1].equals(password[0]) && s[2].equals(password[1]))) {
                ctx.channel().close();
                return;
            }
            //本地服务器的连接
            if (ChannelUtils.add(ctx.channel())) {
                remote = ctx.channel();
                System.out.println("connect");
            } else {
                ctx.channel().close();
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (remote != null && remote == ctx.channel()) {
            ChannelUtils.closed();
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (remote != null && remote == ctx.channel()) {
            ChannelUtils.closed();
        }
        super.exceptionCaught(ctx, cause);
    }
}
