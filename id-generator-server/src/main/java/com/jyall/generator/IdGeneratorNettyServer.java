package com.jyall.generator;

import com.jyall.generator.common.SystemConfig;
import com.jyall.generator.core.ShardIdPreemption;
import com.jyall.generator.netty.IdGeneratorHandler;
import com.jyall.generator.netty.IdRequestDecoder;
import com.jyall.generator.netty.IdResponseEncoder;
import com.jyall.generator.common.zookeeper.ZKClient;
import com.jyall.generator.zookeeper.ZKServerRegister;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * id 生成器 server netty 实现
 * Created by shaojieyue on 11/8/15.
 */
public class IdGeneratorNettyServer implements IdGeneratorServer{
    public static final Logger logger = LoggerFactory.getLogger(IdGeneratorNettyServer.class);
    private Channel serverChannel;
    private InetSocketAddress socketAddress;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private int shardId;
    private ZKServerRegister zkServerRegister;
    private ZKClient zkClient = ZKClient.getClient(SystemConfig.ZK_CONNECTION_STRING);

    public IdGeneratorNettyServer(String host, int port) throws Exception{
        socketAddress = new InetSocketAddress(host, port);
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup(20);
        this.shardId = preemptionShardId(host, port);
        this.zkServerRegister = new ZKServerRegister(zkClient.getCuratorClient(),this.shardId);
    }

    /**
     * 抢占shardId
     * @param host
     * @param port
     * @throws Exception
     */
    private int preemptionShardId(String host, int port) throws Exception{
        final ShardIdPreemption shardIdPreemption = new ShardIdPreemption(zkClient, host,port);
        return shardIdPreemption.preemption();
    }

    public void start(){
        ServerBootstrap bootstrap = new ServerBootstrap(); // (2)
        //保证IdGenerator的单个实例
        final CommonShardIdGenerator commonShardIdGenerator = new CommonShardIdGenerator(this.shardId);
        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class) // (3)
            .childHandler(new ChannelInitializer<SocketChannel>(){ // (4)
                @Override
                public void initChannel(SocketChannel ch) throws Exception{
                    final ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new IdResponseEncoder());//编码器和解码器可以公用实例
                    pipeline.addLast(new IdRequestDecoder());
                    pipeline.addLast(new IdGeneratorHandler(commonShardIdGenerator));
                }
            })
            .option(ChannelOption.SO_BACKLOG, 128)          // (5)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.TCP_NODELAY, true);
        try{
            // Bind and start to accept incoming connections.
            ChannelFuture f = bootstrap.bind(socketAddress).sync(); // (7)
            serverChannel = f.channel();
            logger.info("id generator server started at tcp {}:{}", socketAddress.getHostName(),
                socketAddress.getPort());
            //向注册中心注册服务
            final boolean registerSuccess =
                zkServerRegister.register(socketAddress.getHostName(), socketAddress.getPort());
            if(registerSuccess){
                logger.info("id generator server,shardId={} register success.",shardId);
                // Wait until the server socket is closed.
                // In this example, this does not happen, but you can do that to gracefully
                // shut down your server.
                f.channel().closeFuture().sync();
            }else {//注册失败,关闭服务
                try{
                    this.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }

        }catch(InterruptedException e){
            e.printStackTrace();
        }finally{
            try{
                this.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void close() throws IOException{
        logger.info("closing id generator server.");
        //关闭zkclient 先关闭zk连接,保证服务先从注册中心取消注册
        zkClient.getCuratorClient().close();
        logger.info("close zk client success.");
        if(serverChannel!=null){//关闭server
            serverChannel.close().syncUninterruptibly();
            logger.info("close netty server success.");
        }
        //关闭线程
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        logger.info("close id generator server success.");
    }

    public static void main(String[] args) throws Exception{
        if(args==null||args.length!=2){
            throw new IllegalArgumentException("id generator server must be two param: host port");
        }
        String host = args[0];
        int port = Integer.valueOf(args[1]);

        final IdGeneratorServer idGeneratorServer = new IdGeneratorNettyServer(host, port);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                try{
                    idGeneratorServer.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        });
        idGeneratorServer.start();
    }
}
