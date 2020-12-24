package com.ecspider.common.processor;

import com.ecspider.common.enums.PageItemKeys;
import com.ecspider.common.model.JDModel;
import com.ecspider.common.util.UrlUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
        if (!page.getUrl().regex("https://item.jd.com/[0-9]+.html").match()) {
            // list page
            page.addTargetRequests(page.getHtml().xpath("//*[@id=\"J_goodsList\"]/ul/li/div[1]/div[1]/a/@href").all());

            String nextPageRequest = getNextPageRequest(page);
            if (nextPageRequest != null) {
                page.addTargetRequest(nextPageRequest);
            }

        } else {
            doWithDetailPage(page);
        }
    }

    private void doWithDetailPage(Page page) {
        Document document = page.getHtml().getDocument();
        Elements elements;

        elements = document.getElementsByClass("sku-name");
        if (elements != null && elements.size() != 0) {
            page.putField("title", elements.get(0).text());
        }

        elements = document.getElementsByClass("p-price");
        if (elements != null && elements.size() != 0) {
            Elements prices = elements.get(0).getElementsByTag("span");
            if (prices != null && prices.size() != 0)
            page.putField("price", prices.get(1).text());
        }

        page.putField("shop", page.getHtml().xpath("//*[@id=\"popbox\"]/div/div[1]/h3/a/text()"));

        Element commentCount = document.getElementById("comment-count");
        if (commentCount != null) {
            page.putField("sellCount", commentCount.getElementsByTag("a").get(0).text());
        }

        Elements comments = document.getElementsByClass("comment-con");
        List<String> commentList = new ArrayList<>();
        for (Element comment : comments) {
            commentList.add(comment.text());
        }
        page.putField("commentList", commentList);

    }

    private String getNextPageRequest(Page page) {
        Selectable rawUrl = page.getUrl();
        String url = rawUrl.get();
        System.out.println("url=" + url);
        Map<String, String> params = UrlUtil.getUrlParams(url);
        assert params != null;
        int nextPage = Integer.parseInt(params.get("page")) + 2;
        if (nextPage > 110) {
            return null;
        }
        Integer nextStart = Integer.parseInt(params.get("s")) + 60;
        url = UrlUtil.addParamToUrl(url, "page", String.valueOf(nextPage));
        url = UrlUtil.addParamToUrl(url, "s", String.valueOf(nextStart));
        return url;
    }

    public Site getSite() {
        return site;
    }
}
