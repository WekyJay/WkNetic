package cn.wekyjay.wknetic.common.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import java.util.Locale;

/**
 * 国际化工具类
 */
@Component
public class MessageUtils {

    @Resource
    private MessageSource messageSource;

    private static MessageSource staticMessageSource;

    // 利用 @PostConstruct 将注入的 Bean 赋值给静态变量
    @PostConstruct
    public void init() {
        staticMessageSource = messageSource;
    }

    /**
     * 获取单个消息
     * @param code 资源文件中的 key (例如: code.success)
     */
    public static String get(String code) {
        // 1. "code\\."  -> 强制匹配 "code." (转义点号)
        // 2. "[\\w\\.]+" -> 匹配后续的字母、数字、下划线 (\w) 以及点号 (.)
//        if (!code.matches("code\\.[\\w\\.]+")){
//            return code;
//        }
        return get(code, new Object[]{});
    }

    /**
     * 获取带参数的消息 (例如: 欢迎回来，{0})
     */
    public static String get(String code, Object... args) {
        try {
            // LocaleContextHolder.getLocale() 会自动获取当前请求 Header 中的语言
            return staticMessageSource.getMessage(code, args, Locale.ENGLISH);
        } catch (Exception e) {
            // 如果找不到 key，直接返回 key 本身
            return code;
        }
    }
}