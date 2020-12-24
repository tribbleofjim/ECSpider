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
        Document document = page.getHtml().getDocument();
        int minSize = Integer.MAX_VALUE;
        Elements titles = document.getElementsByClass("p-name p-name-type-2");
        int titlesSize = titles.size();
        minSize = Math.min(minSize, titlesSize);

        Elements prices = document.getElementsByClass("p-price");
        int pricesSize = prices.size();
        minSize = Math.min(minSize, pricesSize);

        Elements commits = document.getElementsByClass("p-commit");
        int commitsSize = commits.size();
        minSize = Math.min(minSize, commitsSize);

        Elements shops = document.getElementsByClass("p-shop");
        int shopsSize = shops.size();
        minSize = Math.min(minSize, shopsSize);

        Elements icons = document.getElementsByClass("p-icons");
        int iconsSize = icons.size();
        minSize = Math.min(minSize, iconsSize);

        List<JDModel> modelList = new ArrayList<>();
        int size;

        for (int i = 0; i < minSize; i++) {
            JDModel jdModel = new JDModel();
            jdModel.setTitle(titles.get(i).getElementsByTag("em").get(0).text());
            String price = prices.get(i).getElementsByTag("i").get(0).text();
            jdModel.setPrice(price);

            int s = commits.get(i).getElementsByTag("a").size();
            String commit = (s > 1) ? commits.get(i).getElementsByTag("a").get(1).text() :
                    commits.get(i).getElementsByTag("a").get(0).text();
            jdModel.setCommit(commit);
            System.out.println(jdModel.getCommit());

            if (shops.get(i).getElementsByTag("a").size() > 0) {
                jdModel.setShop(shops.get(i).getElementsByTag("a").get(0).text());
                System.out.println(jdModel.getShop());
            }

            Elements tempIcons = icons.get(i).getElementsByTag("i");
            StringBuilder builder = new StringBuilder();
            for (Element tempIcon : tempIcons) {
                builder.append(tempIcon.text()).append(" ");
            }
            jdModel.setIcon(builder.toString());
            System.out.println(jdModel.getIcon());

            modelList.add(jdModel);
        }
        page.putField(PageItemKeys.JD_PAGE_KEY.getKey(), modelList);

        String nextPageRequest = getNextPageRequest(page);
        if (nextPageRequest != null) {
            page.addTargetRequest(nextPageRequest);
        }
    }

    private String getNextPageRequest(Page page) {
        Selectable rawUrl = page.getUrl();
        String url = rawUrl.get();
        System.out.println("url=" + url);
        Map<String, String> params = UrlUtil.getUrlParams(url);
        assert params != null;
        Integer nextPage = Integer.parseInt(params.get("page")) + 2;
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
