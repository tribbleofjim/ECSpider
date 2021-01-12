package com.ecspider.common.schedule.spider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * 执行定时spider的任务
 * jobDataMap中必须提供的参数例子：
 * {
 *     "spiderInfo": {
 *          "processor" : "com.ecspider.common.processor.JDProcessor", // 必填
 *          "pipeline" : "com.ecspider.common.pipeline.JDPipeline", // 必填
 *          "urls" : "https://www.sojson.com/editor.html, https://blog.csdn.net/qq_123456/article/details/654321", // 必填
 *          "uuid" : "jd.com", // 必填
 *          "downloader" : "com.ecspider.common.downloader.SeleniumDownloader", // 可选
 *          "threadNum" : 1, // 必填
 *          "maintainUrlNum" : 1000 // 必填
 *     }
 * }
 *
 * @author lyifee
 * on 2021/1/11
 */
public class SpiderJob implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpiderJob.class);

    private TimedSpider timedSpider = null;

    private String spiderInfo;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getTrigger().getJobDataMap();
        String spiderInfo = String.valueOf(jobDataMap.get("spiderInfo"));
        if (timedSpider == null || this.spiderInfo == null || !this.spiderInfo.equals(spiderInfo)) {
            buildSpider(jobExecutionContext);
        }
        timedSpider.executeSpider();
    }

    private void buildSpider(JobExecutionContext jobExecutionContext) {
        JobDataMap jobDataMap = jobExecutionContext.getTrigger().getJobDataMap();
        String spiderInfo;
        if ((spiderInfo = String.valueOf(jobDataMap.get("spiderInfo"))) == null || spiderInfo.equals("null")) {
            LOGGER.error("no_spider_info");
            if (StringUtils.isBlank(this.spiderInfo)) {
                return;
            }
            spiderInfo = this.spiderInfo;
        }
        this.spiderInfo = spiderInfo;

        JSONObject spiderJson = JSON.parseObject(spiderInfo);
        String processorClass = String.valueOf(spiderJson.get("processor"));
        String pipelineClass = String.valueOf(spiderJson.get("pipeline"));
        String urlString = String.valueOf(spiderJson.get("urls"));
        String uuid = String.valueOf(spiderJson.get("uuid"));
        String downloaderClass = String.valueOf(spiderJson.get("downloader"));
        int threadNum = Integer.parseInt(String.valueOf(spiderJson.get("threadNum")));

        int maintain;
        String maintainType = null;
        if (spiderJson.containsKey("maintainUrlNum")) {
            maintainType = "maintainUrlNum";
            maintain = Integer.parseInt((String) spiderJson.get(maintainType));

        } else if (spiderJson.containsKey("maintainTime")){
            maintainType = "maintainTime";
            maintain = Integer.parseInt((String) spiderJson.get(maintainType));

        } else {
            maintain = -1;
        }

        try {
            PageProcessor processor = (PageProcessor) Class.forName(processorClass).newInstance();
            Pipeline pipeline = (Pipeline) Class.forName(pipelineClass).newInstance();
            Downloader downloader = null;
            if (StringUtils.isNotBlank(downloaderClass)) {
                downloader = (Downloader) Class.forName(downloaderClass).newInstance();
            }
            Spider spider = Spider.create(processor)
                    .setUUID(uuid)
                    .addUrl(String.valueOf(urlString))
                    .addPipeline(pipeline)
                    .thread(threadNum);

            if (downloader != null) {
                spider.setDownloader(downloader);
            }

            if (maintainType == null) {
                timedSpider = new TimedSpider(spider);
            } else {
                if (maintainType.equals("maintainUrlNum")) {
                    timedSpider = new TimedSpider(spider).maintainUrls(maintain);
                } else {
                    timedSpider = new TimedSpider(spider).maintainTime(maintain);
                }
            }

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            LOGGER.error("exception_when_building_spider:", e);
        }
    }
}
