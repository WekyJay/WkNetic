package cn.wekyjay.wknetic.admin.system.service;

import cn.wekyjay.wknetic.admin.system.domain.SysOperLog;

public interface ISysOperLogService {
    void insertOperLog(SysOperLog operLog);
}