package com.sec.remote.handler;

import com.sec.remote.util.ChannelUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class LocalReaderHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //本地的连接数量充足
        if (ChannelUtils.count.get() >= ChannelUtils.length) {
            //读取channelKey
            int channelKey = ((ByteBuf) msg).readInt();
            //本地服务器发送来响应，转发给客户端
            Channel client = ChannelUtils.getClientById(channelKey);
            if (client != null) {
                client.writeAndFlush(msg);
            }
        } else {
            ctx.channel().close();
        }
    }
}
