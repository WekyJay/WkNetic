package cn.wekyjay.wknetic.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BusinessType {
    OTHER,
    INSERT,
    UPDATE,
    DELETE,
    SELECT,
    EXPORT,
    IMPORT,
    FORCE,
    GENCODE,
    CLEAN,

}
