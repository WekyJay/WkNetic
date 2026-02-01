package cn.wekyjay.wknetic.common.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体类
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long userId;

    /** 用户名 */
    private String username;

    /** 密码（BCrypt加密） */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 头像地址 */
    private String avatar;

    /** 个人简介 */
    private String bio;

    /** 所在地 */
    private String location;

    /** 个人网站 */
    private String website;

    /** 性别（0未知 1男 2女） */
    private Integer gender;

    /** 状态（0禁用 1启用） */
    private Integer status;

    /** 角色ID（外键关联sys_role.role_id） */
    private Long roleId;

    /** 用户角色（兼容字段，逐步废弃） */
    private String role;

    /** Minecraft账号UUID */
    private String minecraftUuid;

    /** Minecraft游戏名 */
    private String minecraftUsername;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
