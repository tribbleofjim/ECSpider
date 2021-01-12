package com.ecspider.common.schedule.spider;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.SpiderListener;
import java.util.Calendar;
import java.util.Date;

/**
 * @author lyifee
 * on 2021/1/12
 */
public class TimeListener implements SpiderListener {
    private Date endTime;

    private int maintainMinutes;

    private final Spider spider;

    public TimeListener(Spider spider, int maintainMinutes) {
        this.spider = spider;
        this.maintainMinutes = maintainMinutes;

        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, maintainMinutes);
        endTime = now.getTime();
    }

    @Override
    public void onSuccess(Request request) {
        Date now = new Date();
        if (endTime.before(now)) {
            spider.stop();
        }
    }

    @Override
    public void onError(Request request) {

    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getMaintainMinutes() {
        return maintainMinutes;
    }

    public void setMaintainMinutes(int maintainMinutes) {
        this.maintainMinutes = maintainMinutes;
    }
}
