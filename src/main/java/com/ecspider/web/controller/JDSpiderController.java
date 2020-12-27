package com.ecspider.web.controller;

import com.ecspider.web.service.JDSpiderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @RequestMapping("/spider")
    public boolean runJDSpider(@RequestParam(name = "keyword") String keyword,
                               @RequestParam(name = "threadNum") int threadNum) {
        try {
            jdSpiderService.runJDSpider(keyword, threadNum);
            return true;

        } catch (Exception e) {
            LOGGER.error("error_starting_spider:", e);
            return false;
        }
    }
}
