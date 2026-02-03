package cn.wekyjay.wknetic.common.mapper;

import cn.wekyjay.wknetic.common.domain.SysServerToken;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 服务器Token Mapper接口
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Mapper
public interface SysServerTokenMapper extends BaseMapper<SysServerToken> {

    /**
     * 更新最后登录信息
     * 
     * @param tokenValue Token值
     * @param loginIp 登录IP
     * @return 更新行数
     */
    @Update("UPDATE sys_server_token SET last_login_ip = #{loginIp}, last_login_time = NOW() WHERE token_value = #{tokenValue}")
    int updateLastLogin(@Param("tokenValue") String tokenValue, @Param("loginIp") String loginIp);
}
