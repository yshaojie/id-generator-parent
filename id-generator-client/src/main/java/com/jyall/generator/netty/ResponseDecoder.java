package com.jyall.generator.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/**
 * response decoder
 * Created by shaojieyue on 11/8/15.
 */

public class ResponseDecoder extends ByteToMessageDecoder{

    @Override protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf,
        List<Object> list) throws Exception{
        if(byteBuf.readableBytes()>=8){//每次都返回一个long
            list.add(byteBuf.readLong());
        }
    }
}
