package com.ecspider.common.schedule;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * @author lyifee
 * on 2021/1/11
 */
public class SpiderJob implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpiderJob.class);

    private Spider spider = null;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (spider == null) {
            buildSpider(jobExecutionContext);
        }
        spider.run();
    }

    private void buildSpider(JobExecutionContext jobExecutionContext) {
        JobDataMap jobDataMap = jobExecutionContext.getTrigger().getJobDataMap();
        String spiderInfo;
        if ((spiderInfo = String.valueOf(jobDataMap.get("spiderInfo"))) == null || spiderInfo.equals("null")) {
            LOGGER.error("no_spider_info");
            return;
        }

        JSONObject spiderJson = JSON.parseObject(spiderInfo);
        String processorClass = String.valueOf(spiderJson.get("processor"));
        String pipelineClass = String.valueOf(spiderJson.get("pipeline"));

        String urlString = String.valueOf(spiderJson.get("urls"));

        String downloaderClass = String.valueOf(spiderJson.get("processor"));

        int threadNum = Integer.parseInt(String.valueOf(spiderJson.get("threadNum")));
        try {
            PageProcessor processor = (PageProcessor) Class.forName(processorClass).newInstance();
            Pipeline pipeline = (Pipeline) Class.forName(pipelineClass).newInstance();
            Downloader downloader = null;
            if (StringUtils.isNotBlank(downloaderClass)) {
                downloader = (Downloader) Class.forName(downloaderClass).newInstance();
            }
            spider = Spider.create(processor)
                    .addUrl(String.valueOf(urlString))
                    .addPipeline(pipeline)
                    .thread(threadNum);

            if (downloader != null) {
                spider.setDownloader(downloader);
            }

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }
}
