package cn.wekyjay.wknetic.admin.system.service.impl;

import cn.wekyjay.wknetic.admin.system.domain.SysOperLog;
import cn.wekyjay.wknetic.admin.system.mapper.SysOperLogMapper;
import cn.wekyjay.wknetic.admin.system.service.ISysOperLogService;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

@Service
public class SysOperLogServiceImpl implements ISysOperLogService {

    @Resource
    private SysOperLogMapper operLogMapper;

    @Override
    public void insertOperLog(SysOperLog operLog) {
        operLogMapper.insert(operLog);
    }
}