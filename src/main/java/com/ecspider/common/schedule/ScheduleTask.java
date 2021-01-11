package com.ecspider.common.schedule;

import org.springframework.scheduling.Trigger;

/**
 * @author lyifee
 * on 2021/1/11
 */
public interface ScheduleTask {
    Runnable getRunnable();

    Trigger getTrigger();
}
