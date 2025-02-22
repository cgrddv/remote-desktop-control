package cn.yang.puppet.client.netty;

import cn.yang.common.netty.ChannelInitializer;
import cn.yang.common.util.TaskExecutors;
import cn.yang.common.command.Commands;
import cn.yang.common.netty.INettyClient;
import cn.yang.puppet.client.commandhandler.AbstractPuppetCommandHandler;
import cn.yang.puppet.client.constant.ConfigConstants;
import cn.yang.puppet.client.constant.ExceptionMessageConstants;
import cn.yang.common.util.PropertiesUtil;
import cn.yang.puppet.client.exception.PuppetClientException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * @author Cool-Coding
 * 2018/7/24
 */
public class PuppetNettyClient implements INettyClient {
    /**
     * 处理器初始化器
     */
    private ChannelInitializer channelInitialize;

    private NioEventLoopGroup group;

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(PuppetNettyClient.class);

    private String host;
    private int port;

    //与服务器的连结次数
    private int connectionCount=1;

    public void init() throws PuppetClientException{
        group = new NioEventLoopGroup();
        try {
            String pattern = ".*:.*";
            BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
            String str;
            do {
                System.out.println("输入ip + 端口号----------");
                str = bf.readLine();
            } while ("".equals(str) || !Pattern.matches(pattern, str));
            String[] a = str.split(":");
            host = a[0];
            port = Integer.valueOf(a[1]);
        }catch (IOException e){
            throw new PuppetClientException(e.getMessage(),e);
        }
    }

    private void connect () {
        connect("");
    }

    @Override
    public void connect(String server){
            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(channelInitialize);
            if (channelInitialize.getChannelHandler() instanceof PuppetNettyClientHandler) {
                try {
                    final ChannelFuture sync = bootstrap.connect(host, port).sync();
                    sync.channel().writeAndFlush(AbstractPuppetCommandHandler.buildConnectionRequest(connectionCount++));
                    sync.channel().closeFuture().sync();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                    //如果连接断开了，重新与服务器连接
                    try {
                        int interval=PropertiesUtil.getInt(ConfigConstants.CONFIG_FILE_PATH, ConfigConstants.RECONNECT_INTERVAL);
                        LOGGER.error(ExceptionMessageConstants.DISCONNECT_TO_SERVER, host, port,interval);
                        TaskExecutors.submit(this::connect,interval );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }else {
                throw new RuntimeException(ExceptionMessageConstants.PUPPET_HANDLER_ERROR);
            }

    }

    public void setChannelInitialize(ChannelInitializer channelInitialize) {
        this.channelInitialize = channelInitialize;
    }

    public void destroy(){
        group.shutdownGracefully();
    }

}
