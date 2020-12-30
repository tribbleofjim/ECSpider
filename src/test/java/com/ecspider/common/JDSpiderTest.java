package com.ecspider.common;

import com.ecspider.ECApplication;
import com.ecspider.common.downloader.SeleniumDownloader;
import com.ecspider.common.pipeline.JDPipeline;
import com.ecspider.common.processor.JDProcessor;
import com.ecspider.common.util.ConfigUtil;
import com.ecspider.common.util.UrlUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        httpClientDownloader.setProxyProvider(SimpleProxyProvider.from(
                new Proxy("47.97.167.200",3128)
                ,new Proxy("61.153.251.150",22222)));

        Spider.create(new JDProcessor())
                .addUrl("https://search.jd.com/Search?keyword=数码&suggest=1.def.0.base&wq=数码&page=3&s=56&click=0")
                .setDownloader(new SeleniumDownloader())
                .addPipeline(new ConsolePipeline())
                .addPipeline(jdPipeline)
                .thread(1)
                .run();
    }

    @Test
    public void urlUtilTest() {
        String url = "https://search.jd.com/Search?";
        url = UrlUtil.addParamToUrl(url, "keyword", "手机");
        url = UrlUtil.addParamToUrl(url, "suggest", "1.def.0.base");
        url = UrlUtil.addParamToUrl(url, "wq", "手机");
        url = UrlUtil.addParamToUrl(url, "page", String.valueOf(3));
        url = UrlUtil.addParamToUrl(url, "s", String.valueOf(56));
        url = UrlUtil.addParamToUrl(url, "click", "0");
        System.out.println(url);
    }

    @Test
    public void regexTest() {
        String url = "https://club.jd.com/comment/productPageComments.action?callback=fetchJSON_comment98&productId=100009082466&score=3&sortType=6&page=1&pageSize=10&isShadowSku=0&fold=1";
        System.out.println(url.matches("https://club\\.jd\\.com/comment/.*"));
    }

    @Test
    public void decodeTest() {
        String url = "https://search.jd.com/Search?keyword=%E6%95%B0%E7%A0%81&enc=utf-8&wq=%E6%95%B0%E7%A0%81&pvid=9eb460a62e3e4d258cbdff8936a044de";
        try {
            String deUrl = URLDecoder.decode(url, "utf-8");
            System.out.println(deUrl);
            String keyword = UrlUtil.getFromUrl(deUrl, "keyword");
            System.out.println(keyword);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void configUtilTest() {
        System.out.println(ConfigUtil.getValue("application.yml", "props.driver.path"));
    }
}
