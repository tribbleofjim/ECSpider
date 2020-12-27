package com.ecspider.web.service;

import com.ecspider.common.downloader.SeleniumDownloader;
import com.ecspider.common.pipeline.JDPipeline;
import com.ecspider.common.processor.JDProcessor;
import com.ecspider.common.util.UrlUtil;
import com.ecspider.web.SpiderAdvanceCache;
import com.ecspider.web.SpiderExecutorPool;
import com.ecspider.web.model.SpiderAdvance;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;

/**
 * @author lyifee
 * on 2020/12/27
 */
@Service
public class JDSpiderService {
    @Resource
    private JDPipeline jdPipeline;

    private static final String BASE_URL = "https://search.jd.com/Search?";

    public void runJDSpider(String keyword, int threadNum) {
        ExecutorService spiderPool = SpiderExecutorPool.getSpiderPool();
        Spider preparedSpider = Spider.create(new JDProcessor())
                .addUrl(getRootUrl(keyword))
                .setDownloader(new SeleniumDownloader())
                .addPipeline(new ConsolePipeline())
                .addPipeline(jdPipeline)
                .thread(threadNum);
        spiderPool.execute(preparedSpider);
        SpiderAdvance spiderAdvance = new SpiderAdvance();
        spiderAdvance.setKeyword(keyword);
        SpiderAdvanceCache.put(keyword, spiderAdvance);
    }

    private String getRootUrl(String keyword) {
        String url;
        url = UrlUtil.addParamToUrl(BASE_URL, "keyword", keyword);
        url = UrlUtil.addParamToUrl(url, "suggest", "1.def.0.base");
        url = UrlUtil.addParamToUrl(url, "wq", keyword);
        url = UrlUtil.addParamToUrl(url, "page", String.valueOf(1));
        url = UrlUtil.addParamToUrl(url, "s", String.valueOf(1));
        url = UrlUtil.addParamToUrl(url, "click", "0");
        return url;
    }
}
