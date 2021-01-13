package com.ecspider.common.job;

import com.alibaba.fastjson.JSON;
import com.ecspider.common.job.spider.SpiderInfo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author lyifee
 * on 2021/1/13
 */
@Component
public class JobTestBean implements InitializingBean {
    @Resource
    private JobService jobService;

    @Override
    public void afterPropertiesSet() throws Exception {
        QuartzJob quartzJob = new QuartzJob();
        quartzJob.setJobName("test");
        quartzJob.setJobClazz("com.ecspider.common.job.spider.SpiderJob");
        quartzJob.setCronExpression("0 0 0/3 * * ?");
        quartzJob.setStartTime(new Date());

        SpiderInfo spiderInfo = new SpiderInfo();
        spiderInfo.setProcessor("com.ecspider.common.processor.JDProcessor");
        spiderInfo.setPipeline("com.ecspider.common.pipeline.JDPipeline");
        spiderInfo.setUrls("https://search.jd.com/Search?keyword=手机&suggest=1.def.0.base&wq=手机&page=7&s=176&click=0");
        spiderInfo.setUuid("jd.com");
        spiderInfo.setDownloader("com.ecspider.common.downloader.SeleniumDownloader");
        spiderInfo.setThreadNum(1);
        spiderInfo.setMaintainUrlNum(10);

        quartzJob.setExtraInfo(JSON.toJSONString(spiderInfo));

        jobService.addJob(quartzJob);
    }
}
