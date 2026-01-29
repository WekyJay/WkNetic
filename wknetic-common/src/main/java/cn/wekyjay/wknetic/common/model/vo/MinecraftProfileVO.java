package cn.wekyjay.wknetic.common.model.vo;

import lombok.Data;

/**
 * Minecraft账号验证结果
 */
@Data
public class MinecraftProfileVO {
    /** UUID */
    private String id;
    
    /** 用户名 */
    private String name;
    
    /** 是否有效 */
    private Boolean valid;
    
    /** 错误消息 */
    private String error;
}
