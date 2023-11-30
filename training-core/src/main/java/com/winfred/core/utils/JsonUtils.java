package com.winfred.core.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;

/**
 * @author winfred958
 */
public class JsonUtils {

  public static <T> T parseObject(String str, Class<T> clazz) {
    return JSON.parseObject(str, clazz);
  }

  public static String toJsonStr(Object obj) {
    return JSON.toJSONString(obj, JSONWriter.Feature.MapSortField);
  }
}
