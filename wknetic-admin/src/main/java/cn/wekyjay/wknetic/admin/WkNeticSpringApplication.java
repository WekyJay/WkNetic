package cn.wekyjay.wknetic.admin;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "cn.wekyjay.wknetic")
public class WkNeticSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(WkNeticSpringApplication.class, args);
    }
}
