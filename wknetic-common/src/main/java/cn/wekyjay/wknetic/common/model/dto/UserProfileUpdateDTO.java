package cn.wekyjay.wknetic.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 用户资料更新DTO
 * 用于前端更新用户个人资料
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

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
}
