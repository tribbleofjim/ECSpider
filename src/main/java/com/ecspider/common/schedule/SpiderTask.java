package com.ecspider.common.schedule;

import org.springframework.scheduling.Trigger;
import us.codecraft.webmagic.Spider;

/**
 * @author lyifee
 * on 2021/1/11
 */
public class SpiderTask implements ScheduleTask{
    private final Spider spider;

    private final Trigger trigger;

    public SpiderTask(Spider spider, Trigger trigger) {
        this.spider = spider;
        this.trigger = trigger;
    }

    @Override
    public Runnable getRunnable() {
        return spider;
    }

    @Override
    public Trigger getTrigger() {
        return trigger;
    }
}
