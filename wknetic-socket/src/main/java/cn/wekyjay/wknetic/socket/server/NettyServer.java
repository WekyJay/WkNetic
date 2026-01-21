package cn.wekyjay.wknetic.socket.server;

import cn.wekyjay.wknetic.socket.handler.NettyServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

@Slf4j
@Component
public class NettyServer {

    @Value("${wknetic.socket.port:8081}") // 默认端口 8081
    private int port;

    @Resource
    private NettyServerInitializer nettyServerInitializer;

    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public void start() {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(nettyServerInitializer);

            ChannelFuture f = b.bind(port).sync();
            log.info("🚀 WkNetic Socket Server started on port: {}", port);
            
            // 不要在主线程 sync closeFuture，否则会阻塞 Spring 启动
            // f.channel().closeFuture().sync(); 
        } catch (Exception e) {
            log.error("Netty Start Error", e);
        }
    }

    @PreDestroy
    public void stop() {
        log.info("Stopping Netty Server...");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}