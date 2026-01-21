package cn.wekyjay.wknetic.socket;

import cn.wekyjay.wknetic.socket.server.NettyServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class SocketRunner implements CommandLineRunner {

    @Resource
    private NettyServer nettyServer;

    // 👇 加这个构造函数
    public SocketRunner() {
        System.out.println("============================================");
        System.out.println("🆘 SocketRunner 被 Spring 创建了！我活着！");
        System.out.println("============================================");
    }

    @Override
    public void run(String... args) {
        // 另起线程启动 Netty，防止阻塞 Spring 主线程
        new Thread(() -> nettyServer.start()).start();
    }
}