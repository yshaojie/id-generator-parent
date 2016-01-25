package com.jyall.generator.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/**
 * 发送id请求 decoder
 * Created by shaojieyue on 11/8/15.
 */
public class IdRequestDecoder extends ByteToMessageDecoder{

    @Override protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf,
        List<Object> list) throws Exception{
        if(byteBuf.readableBytes()>=4){//每次请求类型都是int值
            list.add(byteBuf.readInt());
        }
    }
}
