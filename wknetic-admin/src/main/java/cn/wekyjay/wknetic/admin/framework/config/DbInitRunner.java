package cn.wekyjay.wknetic.admin.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库自动初始化脚本
 * 启动时自动执行 resources/db/schema.sql
 */
@Slf4j
@Component
public class DbInitRunner implements CommandLineRunner {

    @Resource
    private DataSource dataSource;

    // 开关配置，可以在 application.yml 里关闭自动初始化
    @Value("${wknetic.db-auto-init:true}")
    private boolean autoInit;

    @Override
    public void run(String... args) {
        if (!autoInit) {
            log.info("数据库自动初始化已禁用");
            return;
        }

        log.info("正在检查并初始化数据库表结构...");

        try (Connection connection = dataSource.getConnection()) {
            // 读取 SQL 文件
            ClassPathResource rc = new ClassPathResource("db/schema.sql");
            
            if (connection != null && rc.exists()) {
                // Spring 自带的 ScriptUtils 工具类，非常强大
                // 它会自动处理 SQL 分割、注释清理等
                ScriptUtils.executeSqlScript(connection, rc);
                log.info("✅ 数据库表结构初始化完成 (已跳过已存在的表)");
            } else {
                log.warn("⚠️ 未找到初始化脚本: db/schema.sql");
            }
        } catch (SQLException e) {
            log.error("❌ 数据库初始化失败", e);
        }
    }
}