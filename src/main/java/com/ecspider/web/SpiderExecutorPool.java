package com.ecspider.web;

import us.codecraft.webmagic.Spider;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lyifee
 * on 2020/12/27
 */
public class SpiderExecutorPool {
    private static final ExecutorService SPIDER_POOL = Executors.newCachedThreadPool();

    public static ExecutorService getSpiderPool() {
        return SPIDER_POOL;
    }
}
