package com.ecspider.web.controller;

import com.alibaba.fastjson.JSON;
import com.ecspider.common.job.JobService;
import com.ecspider.common.job.model.QuartzJob;
import com.ecspider.common.job.model.SpiderInfo;
import com.ecspider.common.util.UrlUtil;
import com.ecspider.web.model.Result;
import com.ecspider.web.service.JDSpiderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
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
                                 @RequestParam(name = "maintainUrl") int maintainUrl) throws Exception {
        startTimedSpider(keyword, threadNum, startPage, maintainUrl);
        return Result.success();
    }

    private void startTimedSpider(String keyword, int threadNum, Integer startPage, int maintainUrl) throws Exception {
        QuartzJob quartzJob = new QuartzJob();
        quartzJob.setJobName("jd" + UUID.randomUUID().toString());
        quartzJob.setJobClazz("com.ecspider.common.job.spider.SpiderJob");
        quartzJob.setCronExpression("0 0/5 * * * ?");
        quartzJob.setStartTime(new Date());

        SpiderInfo spiderInfo = new SpiderInfo();
        spiderInfo.setProcessor("com.ecspider.common.processor.JDProcessor");
        spiderInfo.setPipeline("com.ecspider.common.pipeline.JDPipeline");
        if (startPage == null) {
            startPage = 1;
        }
        String url = jdSpiderService.getRootUrl(keyword, startPage);
        spiderInfo.setUrls(url);
        spiderInfo.setUuid("jd.com");
        spiderInfo.setDownloader("com.ecspider.common.downloader.SeleniumDownloader");
        spiderInfo.setThreadNum(threadNum);
        spiderInfo.setMaintainUrlNum(maintainUrl);

        quartzJob.setExtraInfo(JSON.toJSONString(spiderInfo));

        jobService.addJob(quartzJob);
    }
}
