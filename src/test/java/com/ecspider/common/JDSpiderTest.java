package com.ecspider.common;

import com.ecspider.common.downloader.SeleniumDownloader;
import com.ecspider.common.pipeline.MongodbPipeline;
import com.ecspider.common.processor.JDProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;

/**
 * @author lyifee
 * on 2020/12/22
 */
@SpringBootTest(classes = ECApplication.class)
public class JDSpiderTest {
    @Test
    public void jdProcessTest() {
        Spider.create(new JDProcessor())
                .addUrl("https://search.jd.com/Search?keyword=手机&suggest=1.def.0.base&wq=手机&page=3&s=57&click=0")
                .setDownloader(new SeleniumDownloader())
                .addPipeline(new ConsolePipeline())
                .addPipeline(new MongodbPipeline())
                .thread(1)
                .run();
    }
}
