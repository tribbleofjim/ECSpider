package com.ecspider.common.job.spider;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lyifee
 * on 2021/1/14
 */
public class TimedSpiderContainer {
    private static final ConcurrentHashMap<String, TimedSpider> timedSpiderMap = new ConcurrentHashMap<>();

    public static void put(String spiderId, TimedSpider timedSpider) {
        timedSpiderMap.put(spiderId, timedSpider);
    }

    public static TimedSpider get(String spiderId) {
        return timedSpiderMap.getOrDefault(spiderId, null);
    }

    public static void remove(String spiderId) {
        timedSpiderMap.remove(spiderId);
    }
}
