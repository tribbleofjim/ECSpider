package com.ecspider.common.processor;

import com.ecspider.common.enums.PageItemKeys;
import com.ecspider.common.model.JDComment;
import com.ecspider.common.model.JDModel;
import com.ecspider.common.util.UrlUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Json;
import us.codecraft.webmagic.selector.JsonPathSelector;
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

    private static final int RETRY_TIMES = 3;

    private static final int SLEEP_TIME_MILLIS = 5000;

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) " +
            "AppleWebKit/537.31 (KHTML, like Gecko) " +
            "Chrome/26.0.1410.65 " +
            "Safari/537.31";

    private static final String COMMENT_URL = "https://club.jd.com/comment/productPageComments.action?" +
            "callback=fetchJSON_comment98&productId=100009082466&score=3&sortType=6&page=0&pageSize=10&isShadowSku=0&fold=1";

    private Site site = Site.me()
            .setRetryTimes(RETRY_TIMES)
            .setSleepTime(SLEEP_TIME_MILLIS)
            .setUserAgent(USER_AGENT);

    public void process(Page page) {
        if (page.getUrl().regex("https://club\\.jd\\.com/comment/.*").match()) {
            // comment page
            doWithCommentPage(page);
        }
        else {
            doWithListPage(page);
        }
    }

    private void doWithListPage(Page page) {
        grabProductDetails(page);
        addCommentUrls(page);
        String nextUrl = getNextPageRequest(page);
        if (nextUrl != null) {
            page.addTargetRequest(nextUrl);
        }
    }

    private void doWithCommentPage(Page page) {
        String url = page.getUrl().get();
        String skuId = UrlUtil.getFromUrl(url, "productId");
        page.putField("skuId", skuId);
        String score = UrlUtil.getFromUrl(url, "score");
        page.putField("score", score);

        String rawText = page.getRawText();
        System.out.println(rawText);
        if (rawText == null) {
            page.putField("commentList", new ArrayList<JDComment>());
            return;
        }
        String text = rawText.replace(");</body></html>", "")
                .replace("<html><head></head><body>fetchJSON_comment98(", "");

        List<JDComment> commentList = new ArrayList<>();
        int minSize = Integer.MAX_VALUE;
        JsonPathSelector contentSelector = new JsonPathSelector("$.comments[*].content");
        List<String> contents = contentSelector.selectList(text);
        minSize = Math.min(minSize, contents.size());

        JsonPathSelector scoreSelector = new JsonPathSelector("$.comments[*].score");
        List<String> scores = scoreSelector.selectList(text);
        minSize = Math.min(minSize, scores.size());

        JsonPathSelector creationTimeSelector = new JsonPathSelector("$.comments[*].creationTime");
        List<String> creationTimes = creationTimeSelector.selectList(text);
        minSize = Math.min(minSize, creationTimes.size());

        JsonPathSelector productSizeSelector = new JsonPathSelector("$.comments[*].productSize");
        List<String> productSizes = productSizeSelector.selectList(text);
        minSize = Math.min(minSize, productSizes.size());

        for (int i = 0; i < minSize; i++) {
            JDComment comment = new JDComment();
            comment.setContent(contents.get(i));
            comment.setProductType(productSizes.get(i));
            comment.setStar(Integer.parseInt(scores.get(i)));
            comment.setTime(creationTimes.get(i));
            commentList.add(comment);
        }

        page.putField("commentList", commentList);
    }

    private void grabProductDetails(Page page) {
        // get product detail
        Document document = page.getHtml().getDocument();
        int minSize = Integer.MAX_VALUE;
        Elements titles = document.getElementsByClass("p-name p-name-type-2");
        int titlesSize = titles.size();
        minSize = Math.min(minSize, titlesSize);

        Elements prices = document.getElementsByClass("p-price");
        int pricesSize = prices.size();
        minSize = Math.min(minSize, pricesSize);

        List<JDModel> modelList = new ArrayList<>();
        Elements commits = document.getElementsByClass("p-commit");
        int commitsSize = commits.size();
        minSize = Math.min(minSize, commitsSize);

        Elements shops = document.getElementsByClass("p-shop");
        int shopsSize = shops.size();
        minSize = Math.min(minSize, shopsSize);

        Elements icons = document.getElementsByClass("p-icons");
        int iconsSize = icons.size();
        minSize = Math.min(minSize, iconsSize);

        for (int i = 0; i < minSize; i++) {
            JDModel jdModel = new JDModel();
            jdModel.setTitle(titles.get(i).getElementsByTag("em").get(0).text());
            String price = prices.get(i).getElementsByTag("i").get(0).text();
            if (price == null) {
                LOGGER.warn("jd_spider_processor_excption:price is null");
                continue;
            }
            jdModel.setPrice(price);

            int s = commits.get(i).getElementsByTag("a").size();
            String commit = (s > 1) ? commits.get(i).getElementsByTag("a").get(1).text() :
                    commits.get(i).getElementsByTag("a").get(0).text();
            jdModel.setSellCount(commit);

            if (shops.get(i).getElementsByTag("a").size() > 0) {
                jdModel.setShop(shops.get(i).getElementsByTag("a").get(0).text());
            }

            Elements tempIcons = icons.get(i).getElementsByTag("i");
            StringBuilder builder = new StringBuilder();
            for (Element tempIcon : tempIcons) {
                builder.append(tempIcon.text()).append(" ");
            }
            jdModel.setIcon(builder.toString());

            modelList.add(jdModel);
        }
        page.putField(PageItemKeys.JD_PAGE_KEY.getKey(), modelList);
    }

    private void addCommentUrls(Page page) {
        // get skuIds
        List<String> detailUrls = page.getHtml().xpath("//*[@id=\"J_goodsList\"]/ul/li/div[1]/div[1]/a/@href").all();
        List<String> skuIds = new ArrayList<>();
        for (String url : detailUrls) {
            System.out.println(url);
            String skuId = url.replace(".html", "").replace("//item.jd.com/", "");
            skuIds.add(skuId);
        }
        page.putField("skuIds", skuIds);

        // add comment links
        for (String skuId : skuIds) {
            List<String> commentTargets = getCommentRequests(skuId);
            if (CollectionUtils.isNotEmpty(commentTargets)) {
                page.addTargetRequests(commentTargets);
            }
        }
    }

    private String getNextPageRequest(Page page) {
        Selectable rawUrl = page.getUrl();
        String url = rawUrl.get();
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

    private List<String> getCommentRequests(String skuId) {
        if (StringUtils.isBlank(skuId)) {
            return null;
        }
        String url;
        List<String> list = new ArrayList<>();
        url = UrlUtil.addParamToUrl(COMMENT_URL, "productId", skuId);
        // 京东采用数字标明score，1-3分别代表差评，中评，好评
        // 当前只爬取一页好评 + 一页中评 + 一页差评，所以page没有做替换
        for (int i = 1; i <= 3; i++) {
            String tempUrl = UrlUtil.addParamToUrl(url, "score", String.valueOf(i));
            list.add(tempUrl);
        }
        return list;
    }

    public Site getSite() {
        return site;
    }
}
