package com.ecspider.common.schedule.spider;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.SpiderListener;

/**
 * @author lyifee
 * on 2021/1/12
 */
public class UrlNumListener implements SpiderListener {
    private int urlNum = 0;

    private final Spider spider;

    private int stopNum = 1000; // 默认爬取1000条url后停下来

    public UrlNumListener(Spider spider) {
        this.spider = spider;
    }

    public UrlNumListener(Spider spider, int stopNum) {
        this.spider = spider;
        this.stopNum = stopNum;
    }

    @Override
    public void onSuccess(Request request) {
        urlNum++;
        if (urlNum >= stopNum) {
            doStop();
        }
    }

    @Override
    public void onError(Request request) {}

    private void doStop() {
        spider.stop();
    }
}
