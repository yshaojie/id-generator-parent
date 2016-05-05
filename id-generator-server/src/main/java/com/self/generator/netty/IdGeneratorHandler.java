package com.self.generator.netty;

import com.self.generator.core.CommonShardIdGenerator;
import com.self.generator.common.ErrorCodes;
import com.self.generator.common.IdTypes;
import com.self.generator.core.ShortIdGenerator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * id生成server主业务处理handler
 * Created by shaojieyue on 11/8/15.
 */

@ChannelHandler.Sharable
public class IdGeneratorHandler extends SimpleChannelInboundHandler<Integer>{
    private static final Logger logger = LoggerFactory.getLogger(IdGeneratorHandler.class);
    private CommonShardIdGenerator commonShardIdGenerator;
    private ShortIdGenerator shortIdGenerator;
    public IdGeneratorHandler(CommonShardIdGenerator commonShardIdGenerator, ShortIdGenerator shortIdGenerator){
        this.commonShardIdGenerator = commonShardIdGenerator;
        this.shortIdGenerator = shortIdGenerator;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        super.channelActive(ctx);
        logger.info("client connection success,remoteAddress={}",ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("client connection close,remoteAddress={}",ctx.channel().remoteAddress());
    }

    @Override protected void channelRead0(ChannelHandlerContext ctx, Integer idType) throws Exception{
        long id = -1;
        long errorCode = ErrorCodes.SERVER_INTERNAL_ERROR;
        if(idType ==IdTypes.COMMON_ID){//通用id生成器
            id = commonShardIdGenerator.nextId();
        }else if(idType == IdTypes.SHORT_ID) {
            id = shortIdGenerator.nextId();
        }else{//未知请求
            errorCode = ErrorCodes.BAD_REQUEST;
        }
        System.out.println(id);
        if(id > 0){//id生成成功
            ctx.writeAndFlush(id);
        }else {//生成失败,写错误码
            ctx.writeAndFlush(errorCode);
        }
    }

    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        throws Exception{
        logger.error("generator id fail.",cause);
        //返回内部错误码
        ctx.writeAndFlush(ErrorCodes.SERVER_INTERNAL_ERROR);
    }
}
