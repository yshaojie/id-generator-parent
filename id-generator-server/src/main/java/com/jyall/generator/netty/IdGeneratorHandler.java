package com.jyall.generator.netty;

import com.jyall.generator.CommonShardIdGenerator;
import com.jyall.generator.common.ErrorCodes;
import com.jyall.generator.common.IdTypes;
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

    public IdGeneratorHandler(CommonShardIdGenerator commonShardIdGenerator){
        this.commonShardIdGenerator = commonShardIdGenerator;
    }
    @Override public void channelActive(ChannelHandlerContext ctx) throws Exception{
        super.channelActive(ctx);
    }

    @Override protected void channelRead0(ChannelHandlerContext ctx, Integer idType) throws Exception{
        long id = -1;
        long errorCode = ErrorCodes.SERVER_INTERNAL_ERROR;
        if(idType ==IdTypes.COMMON_ID){//通用id生成器
            id = commonShardIdGenerator.nextId();
        }else if(idType == IdTypes.ORDER_ID) {
            errorCode = ErrorCodes.SERVER_INTERNAL_ERROR;
        }else{//未知请求
            errorCode = ErrorCodes.BAD_REQUEST;
        }

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
