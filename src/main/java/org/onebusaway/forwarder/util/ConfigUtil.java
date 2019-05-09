package org.onebusaway.forwarder.util;

import org.apache.commons.lang3.StringUtils;

public class ConfigUtil {

    public static Integer getConfigValue(Integer value, Integer defaultValue){
        if(value == null){
            return defaultValue;
        }
        return value;
    }

    public static String getConfigValue(String value, String defaultValue){
        if(StringUtils.isNotBlank(value)){
            return defaultValue;
        }
        return value;
    }

}
