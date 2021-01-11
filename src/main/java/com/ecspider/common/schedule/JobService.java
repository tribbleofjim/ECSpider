package com.ecspider.common.schedule;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lyifee
 * on 2021/1/11
 */
@Service
public class JobService {
    @Autowired
    @Qualifier("scheduler")
    private Scheduler scheduler;

    /**
     * 新建一个任务
     *
     */
    public String addJob(QuartzJob quartzJob) throws Exception  {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date=df.parse(quartzJob.getStartTime());
        if (!CronExpression.isValidExpression(quartzJob.getCronExpression())) {
            return "Illegal cron expression";   //表达式格式不正确
        }
        JobDetail jobDetail=null;

        //构建job信息
        String clazz = quartzJob.getJobClazz();
        Class<?> jobClazz = Class.forName(clazz);
        jobDetail = JobBuilder.newJob((Class<? extends Job>) jobClazz).withIdentity(quartzJob.getJobName(), quartzJob.getJobClazz()).build();

        //表达式调度构建器(即任务执行的时间,不立即执行)
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(quartzJob.getCronExpression()).withMisfireHandlingInstructionDoNothing();

        //按新的cronExpression表达式构建一个新的trigger
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(quartzJob.getJobName(), quartzJob.getJobClazz()).startAt(date)
                .withSchedule(scheduleBuilder).build();

        //传递参数
        if(quartzJob.getExtraInfo()!=null && !"".equals(quartzJob.getExtraInfo())) {
            trigger.getJobDataMap().put("invokeParam",quartzJob.getExtraInfo());
        }
        scheduler.scheduleJob(jobDetail, trigger);
        // pauseJob(appQuartz.getJobName(),appQuartz.getJobGroup());
        return "success";
    }
}
