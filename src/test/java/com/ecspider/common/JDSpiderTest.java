package com.ecspider.common;

import com.ecspider.common.downloader.SeleniumDownloader;
import com.ecspider.common.pipeline.JDPipeline;
import com.ecspider.common.processor.JDProcessor;
import com.ecspider.common.util.UrlUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;

import java.util.Map;

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
                .addUrl("https://search.jd.com/Search?keyword=手机&suggest=1.def.0.base&wq=手机&page=21&s=596&click=0")
                .setDownloader(new SeleniumDownloader())
                .addPipeline(new ConsolePipeline())
                .addPipeline(jdPipeline)
                .thread(1)
                .run();
    }

    @Test
    public void urlUtilTest() {
        String url = "https://search.jd.com/Search?keyword=手机&suggest=1.def.0.base&wq=手机&page=2&s=61&click=0";
        Map<String, String> params = UrlUtil.getUrlParams(url);
        assert params != null;
        Integer nextPage = Integer.parseInt(params.get("page")) + 2;
        Integer nextStart = Integer.parseInt(params.get("s")) + 60;
        url = UrlUtil.addParamToUrl(url, "page", String.valueOf(nextPage));
        url = UrlUtil.addParamToUrl(url, "s", String.valueOf(nextStart));
        System.out.println(url);
    }

    @Test
    public void regexTest() {
        String url = "https://club.jd.com/comment/productPageComments.action?callback=fetchJSON_comment98&productId=100009082466&score=3&sortType=6&page=1&pageSize=10&isShadowSku=0&fold=1";
        System.out.println(url.matches("https://club\\.jd\\.com/comment/.*"));
    }
}
