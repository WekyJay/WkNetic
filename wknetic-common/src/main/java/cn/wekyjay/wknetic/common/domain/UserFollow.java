package cn.wekyjay.wknetic.common.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户关注关系实体类
 */
@Data
@TableName("user_follow")
public class UserFollow implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关注者用户ID */
    private Long followerId;

    /** 被关注者用户ID */
    private Long followingId;

    /** 关注状态（1关注 0已取消） */
    private Integer status;

    /** 关注时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
