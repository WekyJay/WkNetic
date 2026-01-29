package cn.wekyjay.wknetic.common.model.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户信息返回对象
 */
@Data
public class UserInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 头像地址 */
    private String avatar;

    /** 状态（0禁用 1启用） */
    private Integer status;

    /** 用户角色：ADMIN/MODERATOR/USER/VIP/BANNED */
    private String role;

    /** Minecraft账号UUID */
    private String minecraftUuid;

    /** Minecraft游戏用户名 */
    private String minecraftUsername;
}
