package com.self.generator.netty;

import com.self.generator.AbstarctIdGeneratorClient;
import com.self.generator.core.ConnectionException;
import com.self.generator.core.Request;
import com.self.generator.core.WaitException;
import com.self.generator.core.ResponseFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * id生成器 基于netty实现 Created by shaojieyue on 11/15/15.
 */
public class IdGeneratorNettyClient extends AbstarctIdGeneratorClient{
    public static final Logger logger = LoggerFactory.getLogger(IdGeneratorNettyClient.class);

    private String host;//server host
    private int port;// server port
    private final Bootstrap bootstrap;
    private Channel clientChannel;//连接channel
    private EventLoopGroup workerGroup;

    public IdGeneratorNettyClient(final String host,final int port){
        this.host = host;
        this.port = port;
        workerGroup = new NioEventLoopGroup();
        //最多支持20000个请求在队列里
        final BlockingQueue<ResponseFuture> queue = new LinkedBlockingQueue<ResponseFuture>(20000);
        bootstrap = new Bootstrap(); // (1)
        bootstrap.group(workerGroup); // (2)
        bootstrap.channel(NioSocketChannel.class); // (3)
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true); // (4)
        bootstrap.handler(new ChannelInitializer<SocketChannel>(){
            @Override
            public void initChannel(SocketChannel ch) throws Exception{
                ch.pipeline().addLast(new ResponseDecoder());//请求编码
                ch.pipeline().addLast(new RequestEncoder(queue));//请求编码
                ch.pipeline().addLast(new IdGeneratorClientHandler(queue));//请求业务处理
            }
        });
        // Start the client.
        try{
            connect();//连接服务
        }catch(ConnectionException e){
            e.printStackTrace();
        }
    }

    /**
     * 连接服务
     * @return
     * @throws ConnectionException 连接失败,则抛出连接异常
     */
    private boolean connect() throws ConnectionException{
        boolean success = true;
        try{
            logger.info("start connection server at {}:{}",host,port);
            clientChannel = bootstrap.connect(host, port)
                .addListener(new ConnectionListener(this))//连接监听
                .sync().channel(); // (5)
            //设置连接关闭监听,用来做重连
            clientChannel.closeFuture().addListener(new ReconnectionListener(this));
        }catch(Throwable e){
            logger.info("connection server at {}:{} fail.",host,port);
            throw new ConnectionException(host,port,e);
        }
        logger.info("connection server at {}:{} success.",host,port);
        return success;
    }

    @Override protected long nextId(int idType) throws WaitException{
        final Request request = new Request(idType);
        //发送请求
        clientChannel.writeAndFlush(request);
        //阻塞等待请求结果
        return request.getResponseFuture().get();
    }

    public void close() throws IOException{
        try{
            //同步关闭请求
            clientChannel.close().sync();
        }catch(InterruptedException e){
            logger.error("close connecton ex.",e);
        }
        //关闭线程池
        workerGroup.shutdownGracefully();
    }

    public String getRemoteAddress() {
        return "tcp://"+host+":"+port;
    }

    /**
     * 连接监听
     */
    private class ConnectionListener implements GenericFutureListener<ChannelFuture>{
        private final IdGeneratorNettyClient idGeneratorNettyClient;//id生成器客户端

        public ConnectionListener(IdGeneratorNettyClient idGeneratorNettyClient){
            this.idGeneratorNettyClient = idGeneratorNettyClient;
        }

        public void operationComplete(ChannelFuture channelFuture) throws Exception{
            if(!channelFuture.isSuccess()){//连接失败,订执行重连
                final EventLoop loop = channelFuture.channel().eventLoop();
                loop.schedule(new Runnable(){
                    public void run(){
                        try{
                            idGeneratorNettyClient.connect();//进行连接
                        }catch(Throwable e){
                            logger.error("connecton ex",e);
                        }
                    }
                },1L, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * 重连监听
     */
    private class ReconnectionListener implements GenericFutureListener<ChannelFuture>{
        private final IdGeneratorNettyClient idGeneratorNettyClient;//id生成器客户端

        public ReconnectionListener(IdGeneratorNettyClient idGeneratorNettyClient){
            this.idGeneratorNettyClient = idGeneratorNettyClient;
        }

        public void operationComplete(final ChannelFuture channelFuture) throws Exception{
            logger.warn("id generator connection close,close status={}",channelFuture.isSuccess());
            if(channelFuture.isSuccess()){//连接关闭成功
                final EventLoop loop = channelFuture.channel().eventLoop();
                loop.schedule(new Runnable(){
                    public void run(){
                        try{
                            logger.warn("connection is closed,reconnection id generator server.");
                            idGeneratorNettyClient.connect();//进行重连
                        }catch(Throwable e){
                            logger.error("connecton ex",e);
                        }
                    }
                },1L, TimeUnit.SECONDS);
            }
        }
    }

}
