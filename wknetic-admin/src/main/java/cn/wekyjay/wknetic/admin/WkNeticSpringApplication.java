package cn.wekyjay.wknetic.admin;


import cn.wekyjay.wknetic.socket.SocketRunner;
import cn.wekyjay.wknetic.socket.server.NettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "cn.wekyjay.wknetic")
public class WkNeticSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(WkNeticSpringApplication.class, args);
    }
}
