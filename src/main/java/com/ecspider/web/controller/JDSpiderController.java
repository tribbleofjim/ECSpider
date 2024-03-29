package com.ecspider.web.controller;

import com.alibaba.fastjson.JSON;
import com.ecspider.common.job.JobService;
import com.ecspider.common.job.model.QuartzJob;
import com.ecspider.common.job.model.SpiderInfo;
import com.ecspider.common.util.UrlUtil;
import com.ecspider.web.model.Result;
import com.ecspider.web.service.JDSpiderService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * @author lyifee
 * on 2020/12/27
 */
@Controller
public class JDSpiderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDSpiderController.class);

    @Resource
    private JDSpiderService jdSpiderService;

    @Resource
    private JobService jobService;

    @RequestMapping("/spider")
    @ResponseBody
    public Result runJDSpider(@RequestParam(name = "keyword") String keyword,
                              @RequestParam(name = "threadNum") int threadNum,
                              @RequestParam(name = "startPage", required = false) Integer startPage) {
        try {
            if (startPage != null) {
                jdSpiderService.runJDSpider(keyword, threadNum, startPage);
            } else {
                jdSpiderService.runJDSpider(keyword, threadNum, 1);
            }
            return Result.success("启动成功！");

        } catch (Exception e) {
            LOGGER.error("error_starting_spider:", e);
            return Result.fail(String.format("启动异常，异常信息:%s", e.getMessage()));
        }
    }

    @RequestMapping("/timedSpider")
    @ResponseBody
    public Result runTimedSpider(@RequestParam(name = "keyword") String keyword,
                                 @RequestParam(name = "threadNum") int threadNum,
                                 @RequestParam(name = "startPage", required = false) Integer startPage,
                                 @RequestParam(name = "maintainUrl") int maintainUrl,
                                 @RequestParam(name = "cron") String cron) throws Exception {
        startTimedSpider(keyword, threadNum, startPage, maintainUrl, cron);
        return Result.success();
    }

    private void startTimedSpider(String keyword, int threadNum, Integer startPage, int maintainUrl, String rawCron) throws Exception {
        QuartzJob quartzJob = new QuartzJob();
        int randomPlus = new Random().nextInt(100);

        quartzJob.setJobName("jd" + randomPlus);
        quartzJob.setJobClazz("com.ecspider.common.job.spider.SpiderJob");
        String cron = getCron(rawCron);
        if (StringUtils.isBlank(cron)) {
            LOGGER.error("invalid_cron");
            return;
        }
        System.out.println(cron);
        quartzJob.setCronExpression(cron);
        quartzJob.setStartTime(new Date());

        SpiderInfo spiderInfo = new SpiderInfo();
        spiderInfo.setProcessor("com.ecspider.common.processor.JDProcessor");
        spiderInfo.setPipeline("com.ecspider.common.pipeline.JDPipeline");
        if (startPage == null) {
            startPage = 1;
        }
        String url = jdSpiderService.getRootUrl(keyword, startPage);
        spiderInfo.setUrls(url);
        spiderInfo.setUuid("jd.com" + randomPlus);
        spiderInfo.setDownloader("com.ecspider.common.downloader.SeleniumDownloader");
        spiderInfo.setThreadNum(threadNum);
        spiderInfo.setMaintainUrlNum(maintainUrl);

        quartzJob.setExtraInfo(JSON.toJSONString(spiderInfo));

        jobService.addJob(quartzJob);
    }

    private String getCron(String rawCron) {
        // 5m / 4h
        if (StringUtils.isBlank(rawCron)) {
            LOGGER.error("invalid_rawCron : {}", rawCron);
            return null;
        }
        String baseCron = "0 * * * * ?";
        String num = "0/" + rawCron.substring(0, rawCron.length() - 1);
        if (rawCron.endsWith("m")) {
            return baseCron.replaceFirst("\\*", num);

        } else if (rawCron.endsWith("h")){
            String cron = baseCron.replaceFirst("\\*", "0");
            cron = cron.replaceFirst("\\*", num);
            return cron;

        } else {
            throw new RuntimeException("invalid_rawCron : " + rawCron);
        }
    }
}
