package cn.wekyjay.wknetic.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
public class I18nConfig {

    /**
     * 1. 告诉 Spring Boot 去 i18n 目录下找 messages 开头的文件
     */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        // 指定基础名称 (src/main/resources/i18n/messages)
        source.setBasenames("i18n/messages");
        // 设置编码，防止中文乱码
        source.setDefaultEncoding(StandardCharsets.UTF_8.name());
        source.setUseCodeAsDefaultMessage(true);
        // 禁止回退到系统区域设置（防止开发环境是中文导致默认显示中文）
        source.setFallbackToSystemLocale(false);
        return source;
    }

    /**
     * 2. 解析器：根据 HTTP 请求头 "Accept-Language" 自动切换语言
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        // 默认语言：英文
        resolver.setDefaultLocale(Locale.US);
        return resolver;
    }

    /**
     * 3. 【新增】关键配置：让 @NotBlank 等注解使用我们的 MessageSource
     */
    @Bean
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        // 核心：把校验器的“字典”指向我们自定义的 messageSource
        bean.setValidationMessageSource(messageSource());
        return bean;
    }
}