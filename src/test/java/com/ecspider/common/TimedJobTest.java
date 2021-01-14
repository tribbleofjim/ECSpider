package com.ecspider.common;

import com.alibaba.fastjson.JSON;
import com.ecspider.ECApplication;
import com.ecspider.common.job.JobService;
import com.ecspider.common.job.model.QuartzJob;
import com.ecspider.common.job.model.SpiderInfo;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

/**
 * @author lyifee
 * on 2021/1/13
 */
@SpringBootTest(classes = ECApplication.class)
public class TimedJobTest {
    @Autowired
    private JobService jobService;

    @Test
    public void jobServiceTest() throws Exception {
        QuartzJob quartzJob = new QuartzJob();
        quartzJob.setJobName("test");
        quartzJob.setJobClazz("com.ecspider.common.job.spider.SpiderJob");
        quartzJob.setCronExpression("0 0/3 * * * ?");
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

    @Test
    public void quartzTest() throws SchedulerException {
        //创建一个scheduler
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.getContext().put("skey", "svalue");

        //创建一个Trigger
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger1", "group1")
                .usingJobData("t1", "tv1")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(3)
                        .repeatForever()).build();
        trigger.getJobDataMap().put("t2", "tv2");

        //创建一个job
        JobDetail job = JobBuilder.newJob(HelloJob.class)
                .usingJobData("j1", "jv1")
                .withIdentity("myjob", "mygroup").build();
        job.getJobDataMap().put("j2", "jv2");

        //注册trigger并启动scheduler
        scheduler.scheduleJob(job,trigger);
        scheduler.start();
    }
}
