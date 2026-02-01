package cn.wekyjay.wknetic.common.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户公开资料VO
 * 用于返回给前端的公开用户信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

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

    /** 粉丝数 */
    private Long followerCount;

    /** 关注数 */
    private Long followingCount;

    /** 帖子数 */
    private Long postCount;

    /** 当前用户是否已关注此用户 */
    private Boolean isFollowing;

    /** 创建时间 */
    private Date createTime;
}
