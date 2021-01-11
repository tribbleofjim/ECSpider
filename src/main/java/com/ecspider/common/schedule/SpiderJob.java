package com.ecspider.common.schedule;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import us.codecraft.webmagic.Spider;

/**
 * @author lyifee
 * on 2021/1/11
 */
public class SpiderJob implements Job {

    private Spider spider;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        spider.run();
    }
}
