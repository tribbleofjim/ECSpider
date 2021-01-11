package com.ecspider.common.schedule;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import us.codecraft.webmagic.Spider;

/**
 * @author lyifee
 * on 2021/1/11
 */
public class SpiderJob extends QuartzJobBean {

    private Spider spider;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        spider.run();
    }
}
