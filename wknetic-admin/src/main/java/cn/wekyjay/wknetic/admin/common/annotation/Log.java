package cn.wekyjay.wknetic.admin.common.annotation;

import cn.wekyjay.wknetic.common.enums.BusinessType;
import java.lang.annotation.*;

@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 模块名称 (例如：用户管理)
     */
    String title() default "";

    /**
     * 功能类型 (例如：INSERT, UPDATE)
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * 是否保存请求的参数
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存响应数据
     */
    boolean isSaveResponseData() default true;
}