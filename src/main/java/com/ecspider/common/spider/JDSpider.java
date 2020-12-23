package com.ecspider.common.spider;

import com.ecspider.common.downloader.SeleniumDownloader;
import com.ecspider.common.pipeline.JDPipeline;
import com.ecspider.common.processor.JDProcessor;
import us.codecraft.webmagic.Spider;

import javax.annotation.Resource;

/**
 * @author lyifee
 * on 2020/12/23
 */
public class JDSpider implements SpiderTask{
    private static final Integer THREAD_NUMS = 2;

    private static final String SITE_ROOT_URL = "https://search.jd.com/Search";

    @Resource
    private JDPipeline jdPipeline;

    @Override
    public void execute() {
        Spider.create(new JDProcessor())
            .addUrl("https://search.jd.com/Search?keyword=手机&suggest=1.def.0.base&wq=手机&page=5&s=57&click=0")
            .setDownloader(new SeleniumDownloader())
            // .addPipeline(new ConsolePipeline())
            .addPipeline(jdPipeline)
            .thread(THREAD_NUMS)
            .run();
    }

}
