package com.ecspider.common.processor;

import com.ecspider.common.enums.PageItemKeys;
import com.ecspider.common.model.JDModel;
import com.ecspider.common.util.UrlUtil;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lyifee
 * on 2020/12/21
 */
public class JDProcessor implements PageProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDProcessor.class);

    private static final Integer RETRY_TIMES = 3;

    private static final Integer SLEEP_TIME_MILLIS = 5000;

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) " +
            "AppleWebKit/537.31 (KHTML, like Gecko) " +
            "Chrome/26.0.1410.65 " +
            "Safari/537.31";

    private Site site = Site.me()
            .setRetryTimes(RETRY_TIMES)
            .setSleepTime(SLEEP_TIME_MILLIS)
            .setUserAgent(USER_AGENT);

    public void process(Page page) {
        Document document = page.getHtml().getDocument();
        Elements titles = document.getElementsByClass("p-name p-name-type-2");
        int titlesSize = titles.size();

        Elements prices = document.getElementsByClass("p-price");
        int pricesSize = prices.size();

        List<JDModel> modelList = new ArrayList<>();

        for (int i = 0; i < titlesSize; i++) {
            if (i > pricesSize) {
                break;
            }

            JDModel jdModel = new JDModel();
            jdModel.setTitle(titles.get(i).getElementsByTag("em").get(0).text());
            String price = prices.get(i).getElementsByTag("i").get(0).text();
            if (price == null) {
                LOGGER.warn("jd_spider_processor_excption:price is null");
                continue;
            }
            jdModel.setPrice(price);

            modelList.add(jdModel);
        }
        page.putField(PageItemKeys.JD_PAGE_KEY.getKey(), modelList);

        page.addTargetRequest(getNextPageRequest(page));
    }

    public String getNextPageRequest(Page page) {
        Selectable rawUrl = page.getUrl();
        String url = rawUrl.get();
        Map<String, String> params = UrlUtil.getUrlParams(url);
        assert params != null;
        Integer nextPage = Integer.parseInt(params.get("page")) + 2;
        Integer nextStart = Integer.parseInt(params.get("s")) + 60;
        UrlUtil.addParamToUrl(url, "page", String.valueOf(nextPage));
        UrlUtil.addParamToUrl(url, "s", String.valueOf(nextStart));
        return url;
    }

    public Site getSite() {
        return site;
    }
}
