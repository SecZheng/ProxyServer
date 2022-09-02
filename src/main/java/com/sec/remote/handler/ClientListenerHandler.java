package com.sec.remote.handler;

import com.sec.remote.util.ChannelUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientListenerHandler extends ChannelInboundHandlerAdapter {

    //为每个连接的客户端添加编号
    private static AtomicInteger i = new AtomicInteger(0);
    //待复用的编号
    private static TreeSet<Integer> treeSet = new TreeSet<>();

    //通道编号
    int channelKey;

    {
        if (treeSet.isEmpty()) {
            channelKey = i.getAndIncrement();
        } else {
            synchronized (treeSet) {
                if (treeSet.isEmpty()) {
                    channelKey = i.getAndIncrement();
                } else {
                    channelKey = treeSet.pollFirst();
                }
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel remote = ChannelUtils.getRemoteById(channelKey, ctx.channel());
        //本地服务器开着则转发请求
        if (remote != null) {
            ByteBuf buf = ctx.alloc().buffer(4);
            buf.writeInt(((ByteBuf) msg).readableBytes());
            buf.writeInt(channelKey);
            remote.writeAndFlush(buf);
            remote.writeAndFlush(msg);
        } else {
            ctx.channel().close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelUtils.remove(channelKey);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ChannelUtils.remove(channelKey);
        super.exceptionCaught(ctx, cause);
    }
}
