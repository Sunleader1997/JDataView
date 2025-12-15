package org.sunyaxing.imagine.jdataviewserver.entity.cover;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.mapstruct.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


@Named("CommonCover")
public class CommonCover {
    private static final Logger log = LoggerFactory.getLogger(CommonCover.class);

    @Named("generateIfNull")
    public String generateIfNull(String id) {
        return id == null ? IdUtil.getSnowflakeNextIdStr() : id;
    }

    @Named("strToProperties")
    public JSONObject strToProperties(String properties) {
        try {
            return JSONObject.parseObject(properties);
        } catch (Exception e) {
            log.error("json字符串转换异常", e);
            return null;
        }
    }

    @Named("propertiesToStr")
    public String propertiesToStr(JSONObject properties) {
        return JSONObject.toJSONString(properties);
    }


    @Named("useSet")
    public <T> T useSet(T data) {
        return data;
    }

    @Named("emptyIfNull")
    public List emptyIfNull(List list) {
        if (list == null) {
            return new ArrayList();
        }
        return list;
    }

    @Named("intToPx")
    public String intToPx(Integer px) {
        if (px != null) {
            return px + "px";
        }
        return null;
    }

    @Named("stringToArray")
    public List<String> stringToArray(String arrayStr) {
        return JSONArray.parseArray(arrayStr, String.class);
    }

    @Named("arrayToString")
    public String arrayToString(List<String> arrayStr) {
        return JSONObject.toJSONString(arrayStr);
    }
}
