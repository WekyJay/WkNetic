package cn.wekyjay.wknetic.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "cn.wekyjay.wknetic")
@MapperScan("cn.wekyjay.wknetic.**.mapper")
public class WkNeticSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(WkNeticSpringApplication.class, args);
    }
}
