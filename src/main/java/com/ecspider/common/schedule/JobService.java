package com.ecspider.common.schedule;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
        Class<? extends Job> jobClazz = (Class<? extends Job>) Class.forName(clazz);
        jobDetail = JobBuilder.newJob(jobClazz).withIdentity(quartzJob.getJobName(), quartzJob.getJobClazz()).build();

        //表达式调度构建器(即任务执行的时间,不立即执行)
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(quartzJob.getCronExpression()).withMisfireHandlingInstructionDoNothing();

        //按新的cronExpression表达式构建一个新的trigger
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(quartzJob.getJobName(), quartzJob.getJobClazz()).startAt(date)
                .withSchedule(scheduleBuilder).build();

        //传递参数
        if(quartzJob.getExtraInfo()!=null && !"".equals(quartzJob.getExtraInfo())) {
            trigger.getJobDataMap().put("extraInfo",quartzJob.getExtraInfo());
        }
        scheduler.scheduleJob(jobDetail, trigger);
        return "success";
    }

    public List<QuartzJob> getAllJob() throws SchedulerException {
        // TODO : 用mysql存储元数据信息
        return null;
    }

    /**
     * 获取Job状态
     * @param jobName
     * @param jobGroup
     * @return
     * @throws SchedulerException
     */
    public String getJobState(String jobName, String jobGroup) throws SchedulerException {
        TriggerKey triggerKey = new TriggerKey(jobName, jobGroup);
        return scheduler.getTriggerState(triggerKey).name();
    }

    //暂停所有任务
    public void pauseAllJob() throws SchedulerException {
        scheduler.pauseAll();
    }

    //暂停任务
    public String pauseJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, jobGroup);
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        if (jobDetail == null) {
            return "fail";
        }else {
            scheduler.pauseJob(jobKey);
            return "success";
        }

    }

    //恢复所有任务
    public void resumeAllJob() throws SchedulerException {
        scheduler.resumeAll();
    }

    // 恢复某个任务
    public String resumeJob(String jobName, String jobGroup) throws SchedulerException {

        JobKey jobKey = new JobKey(jobName, jobGroup);
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        if (jobDetail == null) {
            return "fail";
        }else {
            scheduler.resumeJob(jobKey);
            return "success";
        }
    }

    //删除某个任务
    public String  deleteJob(QuartzJob quartzJob) throws SchedulerException {
        JobKey jobKey = new JobKey(quartzJob.getJobName(), quartzJob.getJobClazz());
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        if (jobDetail == null ) {
            return "jobDetail is null";
        }else if(!scheduler.checkExists(jobKey)) {
            return "jobKey is not exists";
        }else {
            scheduler.deleteJob(jobKey);
            return "success";
        }

    }

    //修改任务
    public String  modifyJob(QuartzJob quartzJob) throws SchedulerException {
        if (!CronExpression.isValidExpression(quartzJob.getCronExpression())) {
            return "Illegal cron expression";
        }
        TriggerKey triggerKey = TriggerKey.triggerKey(quartzJob.getJobName(),quartzJob.getJobClazz());
        JobKey jobKey = new JobKey(quartzJob.getJobName(),quartzJob.getJobClazz());
        if (scheduler.checkExists(jobKey) && scheduler.checkExists(triggerKey)) {
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            //表达式调度构建器,不立即执行
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(quartzJob.getCronExpression()).withMisfireHandlingInstructionDoNothing();
            //按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            //修改参数
            if(!trigger.getJobDataMap().get("extraInfo").equals(quartzJob.getExtraInfo())) {
                trigger.getJobDataMap().put("extraInfo",quartzJob.getExtraInfo());
            }
            //按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
            return "success";
        }else {
            return "job or trigger not exists";
        }

    }
}
