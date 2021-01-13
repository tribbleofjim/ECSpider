package com.ecspider.common;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import java.time.LocalDateTime;

/**
 * @author lyifee
 * on 2021/1/13
 */
public class HelloJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Object tv1 = context.getTrigger().getJobDataMap().get("t1");
        Object tv2 = context.getTrigger().getJobDataMap().get("t2");
        Object jv1 = context.getJobDetail().getJobDataMap().get("j1");
        Object jv2 = context.getJobDetail().getJobDataMap().get("j2");
        Object sv = null;
        try {
            sv = context.getScheduler().getContext().get("skey");
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        System.out.println(tv1+":"+tv2);
        System.out.println(jv1+":"+jv2);
        System.out.println(sv);
        System.out.println("hello:"+ LocalDateTime.now());
    }

}
