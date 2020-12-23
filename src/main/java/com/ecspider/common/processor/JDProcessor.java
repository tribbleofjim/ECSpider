package com.ecspider.common.processor;

import com.ecspider.common.enums.PageItemKeys;
import com.ecspider.common.model.JDModel;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.List;

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
            System.out.println(i);
            System.out.println(price);
            if (price == null) {
                System.out.println("price is null");
                continue;
            }
            jdModel.setPrice(price);

            modelList.add(jdModel);
        }
        page.putField(PageItemKeys.JD_PAGE_KEY.getKey(), modelList);
    }

    public Site getSite() {
        return site;
    }
}
