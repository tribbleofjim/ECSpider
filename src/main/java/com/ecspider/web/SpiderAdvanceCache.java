package com.ecspider.web;

import com.ecspider.web.model.SpiderAdvance;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lyifee
 * on 2020/12/27
 */
public class SpiderAdvanceCache {
    private static final ConcurrentHashMap<String, SpiderAdvance> ADVANCE_CACHE = new ConcurrentHashMap<>();

    public static void put(String keyword, SpiderAdvance spiderAdvance) {
        ADVANCE_CACHE.put(keyword, spiderAdvance);
    }

    public static SpiderAdvance get(String keyword) {
        return ADVANCE_CACHE.getOrDefault(keyword, null);
    }

    public static void remove(String keyword) {
        ADVANCE_CACHE.remove(keyword);
    }
}
