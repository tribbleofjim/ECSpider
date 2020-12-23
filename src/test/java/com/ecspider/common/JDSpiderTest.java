package com.ecspider.common;

import com.ecspider.common.downloader.SeleniumDownloader;
import com.ecspider.common.pipeline.JDPipeline;
import com.ecspider.common.processor.JDProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;

/**
 * @author lyifee
 * on 2020/12/22
 */
@SpringBootTest(classes = ECApplication.class)
public class JDSpiderTest {
    @Autowired
    private JDPipeline jdPipeline;

    @Test
    public void jdProcessTest() {
        Spider.create(new JDProcessor())
                .addUrl("https://search.jd.com/Search?keyword=手机&suggest=1.def.0.base&wq=手机&page=5&s=57&click=0")
                .setDownloader(new SeleniumDownloader())
                .addPipeline(new ConsolePipeline())
                .addPipeline(jdPipeline)
                .thread(1)
                .run();
    }
}
