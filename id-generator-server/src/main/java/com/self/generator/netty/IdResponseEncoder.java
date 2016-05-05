package com.self.generator.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by shaojieyue on 11/8/15.
 */
public class IdResponseEncoder extends MessageToByteEncoder<Long>{
    @Override protected void encode(ChannelHandlerContext ctx, Long msg, ByteBuf out)
        throws Exception{
        out.writeLong(msg);
    }
}
