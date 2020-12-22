package com.ecspider.common.processor;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * @author lyifee
 * on 2020/12/21
 */
public class JDProcessor implements PageProcessor {
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
        // TODO : 将下一页链接加入page
        Document document = page.getHtml().getDocument();
        Elements elements = document.getElementsByClass("gl-item");
        int pageSize = elements.size();

        Elements titles = document.getElementsByClass("p-name p-name-type-2");
        int titlesSize = titles.size();

        for (int i = 0; i < pageSize; i++) {
            if (i > titlesSize) {
                break;
            }
            page.putField("title", titles.get(i).getElementsByTag("em").get(0).text());
            String rawPrice = page.getHtml().xpath("//*[@id=\"J_goodsList\"]/ul/li[" + i + "]/div/div[3]/strong/i").toString();
            page.putField("price", rawPrice.replace("<i>", ""));
        }
    }

    public Site getSite() {
        return site;
    }
}
