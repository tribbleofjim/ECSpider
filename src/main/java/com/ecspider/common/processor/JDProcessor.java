package com.ecspider.common.processor;

import com.ecspider.common.model.JDComment;
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
        if (page.getUrl().regex("https://item.jd.com/[0-9]+.html").match()) {
            // detail page
            doWithDetailPage(page);

        } else if (page.getUrl().regex("https://club\\.jd\\.com/comment/.*").match()) {
            // comment page
            doWithCommentPage(page);
        }
        else {
            // list page
            page.addTargetRequests(page.getHtml().xpath("//*[@id=\"J_goodsList\"]/ul/li/div[1]/div[1]/a/@href").all());

            String nextPageRequest = getNextPageRequest(page);
            if (nextPageRequest != null) {
                Request request = new Request();
                request.setUrl(nextPageRequest);
                page.addTargetRequest(request);
            }
        }
    }

    private void doWithDetailPage(Page page) {
        Document document = page.getHtml().getDocument();
        Elements elements;

        Elements reportBtn = document.getElementsByClass("report-btn");
        if (reportBtn == null) {
            return;
        }
        Element reportContent = reportBtn.get(0);
        if (reportContent == null) {
            return;
        }
        String skuHref = reportContent.attr("href");
        String skuId = null;
        if (StringUtils.isNotBlank(skuHref)) {
            skuId = UrlUtil.getFromUrl(skuHref, "skuId");
            if (StringUtils.isNotBlank(skuId)) {
                page.putField("skuId", skuId);
            }
        }

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

        String shop = null;
        Elements shops = document.getElementsByClass("item");
        if (shops != null) {
            shop = shops.get(0).getElementsByClass("name").get(0).text();
        }
        page.putField("shop", shop);

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

        // add comment links
        List<String> commentTargets = getCommentRequests(skuId);
        if (CollectionUtils.isNotEmpty(commentTargets)) {
            page.addTargetRequests(commentTargets);
        }
    }

    private void doWithCommentPage(Page page) {
        Json json = page.getJson();
        if (json == null) {
            page.putField("commentList", new ArrayList<JDComment>());
            return;
        }
        String text = json.get();
        System.out.println(text);

        List<JDComment> commentList = new ArrayList<>();
        int minSize = Integer.MAX_VALUE;
        JsonPathSelector contentSelector = new JsonPathSelector("$.fetchJSON_comment98.comments[*].content");
        List<String> contents = contentSelector.selectList(text);
        minSize = Math.min(minSize, contents.size());

        JsonPathSelector scoreSelector = new JsonPathSelector("$.fetchJSON_comment98.comments[*].score");
        List<String> scores = scoreSelector.selectList(text);
        minSize = Math.min(minSize, scores.size());

        JsonPathSelector creationTimeSelector = new JsonPathSelector("$.fetchJSON_comment98.comments[*].creationTime");
        List<String> creationTimes = creationTimeSelector.selectList(text);
        minSize = Math.min(minSize, creationTimes.size());

        JsonPathSelector productSizeSelector = new JsonPathSelector("$.fetchJSON_comment98.comments[*].productSize");
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
