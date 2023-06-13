package io.shulie.amdb.common;

import org.apache.commons.lang.StringUtils;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/6/13 11:16
 */
public enum CalcType {
    SUM("SUM"),
    AVG("MEAN"),
    COUNT("COUNT"),
    MAX("MAX"),
    MIN("MIN"),
    MEDIAN("MEDIAN"),
    ;

    private String calcType;

    CalcType(String calcType) {
        this.calcType = calcType;
    }

    public static CalcType get(String calcType) {
        for (CalcType type : CalcType.values()) {
            if (StringUtils.equalsIgnoreCase(type.name(), calcType)
                    || StringUtils.equalsIgnoreCase(type.getCalcType(), calcType)) {
                return type;
            }
        }
        return null;
    }

    public String getCalcType() {
        return calcType;
    }
}
