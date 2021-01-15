package com.ecspider.web.controller;

import com.ecspider.common.job.JobService;
import com.ecspider.web.model.Result;
import com.ecspider.web.service.JDSpiderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

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

    @RequestMapping
    @ResponseBody
    public Result runTimedSpider(@RequestParam(name = "keyword") String keyword,
                                 @RequestParam(name = "threadNum") int threadNum,
                                 @RequestParam(name = "startPage", required = false) Integer startPage) {
        return Result.success();
    }
}
