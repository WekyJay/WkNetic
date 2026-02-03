package cn.wekyjay.wknetic.admin.system.service;

import cn.wekyjay.wknetic.common.domain.SysServerToken;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 服务器Token Service接口
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
public interface ISysServerTokenService extends IService<SysServerToken> {

    /**
     * 分页查询Token列表
     * 
     * @param page 分页对象
     * @param name Token名称（模糊查询）
     * @param status 状态
     * @return 分页结果
     */
    Page<SysServerToken> getTokenPage(Page<SysServerToken> page, String name, Integer status);

    /**
     * 创建Token
     * 
     * @param name Token名称
     * @param remark 备注
     * @param createBy 创建人
     * @return Token对象
     */
    SysServerToken createToken(String name, String remark, String createBy);

    /**
     * 重新生成Token值
     * 
     * @param id Token ID
     * @return 新的Token值
     */
    String regenerateToken(Long id);

    /**
     * 更新Token状态
     * 
     * @param id Token ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
}
