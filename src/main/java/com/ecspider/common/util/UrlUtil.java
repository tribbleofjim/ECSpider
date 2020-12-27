package com.ecspider.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author lyifee
 * on 2020/12/23
 */
public class UrlUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlUtil.class);

    public static Map<String, String> getUrlParams(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        String[] pieces = splitUrl(url);
        if (pieces == null) {
            return null;
        }

        HashMap<String, String> map = new HashMap<>();
        String[] params = pieces[1].split("&");
        for (String param : params) {
            if (StringUtils.isBlank(param)) {
                continue;
            }
            String[] keyValue = param.split("=");
            map.put(keyValue[0], keyValue[1]);
        }
        return map;
    }

    public static String getBaseUrl(String url) {
        return Objects.requireNonNull(splitUrl(url))[0];
    }

    public static String getFromUrl(String url, String paramKey) {
        Map<String, String> paramsMap = getUrlParams(url);
        if (paramsMap == null) {
            return null;
        }
        return paramsMap.getOrDefault(paramKey, null);
    }

    public static String addParamToUrl(String url, String key, String value) {
        if (StringUtils.isBlank(url)) {
            return url;
        }

        Map<String, String> paramsMap = getUrlParams(url);
        if (paramsMap != null && paramsMap.containsKey(key)) {
            return url.replace(getParamByKeyValue(key, paramsMap.get(key)),
                    getParamByKeyValue(key, value));

        } else {
            if (url.endsWith("?")) {
                return url + getParamByKeyValue(key, value);
            }
            return url + "&" + getParamByKeyValue(key, value);
        }
    }

    private static String[] splitUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        String[] pieces = url.split("\\?");
        if (pieces.length != 2) {
            LOGGER.error("analyzeUtil_error:invalid_url:{}", url);
            return null;
        }
        return pieces;
    }

    private static String getParamByKeyValue(String key, String value) {
        return key + "=" + value;
    }

}
