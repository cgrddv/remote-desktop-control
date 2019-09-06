package cn.yang.server.netty;

import cn.yang.common.util.PropertiesUtil;
import cn.yang.server.constant.ConfigConstants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * @author Cool-Coding 2018/7/24
 */
public class NettyServer {
    /**
     * 处理器初始化器
     */
    private ChannelHandler channelInitialize;


    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);

    private void bind(String host,int port) throws InterruptedException{
        final NioEventLoopGroup boss=new NioEventLoopGroup();
        final NioEventLoopGroup worker=new NioEventLoopGroup();

        try{
            final ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss,worker)
                     .channel(NioServerSocketChannel.class)
                     .option(ChannelOption.SO_BACKLOG,1024)
                     .childOption(ChannelOption.SO_KEEPALIVE,true)
                     .childHandler(channelInitialize);

            final ChannelFuture f = bootstrap.bind(host, port).sync();

            LOGGER.info("server start on port:{}",port);
            f.channel().closeFuture().sync();
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }


    public void start() throws Exception{
        try {
            String pattern = ".*:.*";
            BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
            String str;
            do {
                System.out.println("输入自己网络能访问到的ip + 端口号");
                str = bf.readLine();
            } while ("".equals(str) || !Pattern.matches(pattern, str));
            String[] a = str.split(":");
            String ip = a[0];
            int port = Integer.valueOf(a[1]);
            bind(ip,port);
        }catch (IOException | InterruptedException e){
            LOGGER.error(e.getMessage(),e);
            throw e;
        }
    }

    public void setChannelInitialize(ChannelHandler channelInitialize) {
        this.channelInitialize = channelInitialize;
    }
}
