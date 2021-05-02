package com.ecspider.common.processor;

import com.ecspider.common.enums.PageItemKeys;
import com.ecspider.common.model.JDComment;
import com.ecspider.common.model.JDModel;
import com.ecspider.common.util.UrlUtil;
import com.ecspider.web.SpiderAdvanceCache;
import com.ecspider.web.model.SpiderAdvance;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.JsonPathSelector;
import us.codecraft.webmagic.selector.Selectable;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author lyifee
 * on 2020/12/21
 */
public class JDProcessor implements PageProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDProcessor.class);

    private static final int RETRY_TIMES = 3;

    private static final int SLEEP_TIME_MILLIS = 5000;

    private static final int DEFAULT_PAGE_NUM = 60;

    private static final String JD_REFERER = "https://item.jd.com/";

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) " +
            "AppleWebKit/537.31 (KHTML, like Gecko) " +
            "Chrome/26.0.1410.65 " +
            "Safari/537.31";

    private static final String COMMENT_URL = "https://club.jd.com/comment/productPageComments.action?" +
            "callback=fetchJSON_comment98&productId=100009082466&score=3&sortType=5&page=0&pageSize=10&isShadowSku=0&fold=1";

    private Site site = Site.me()
            .setRetryTimes(RETRY_TIMES)
            .setSleepTime(SLEEP_TIME_MILLIS)
            .setUserAgent(USER_AGENT)
            .addHeader("Referer", JD_REFERER)
            ;

    public void process(Page page) {
        if (page.getUrl().regex("https://club\\.jd\\.com/comment/.*").match()) {
            // comment page
            try {
                doWithCommentPage(page);
            } catch (InterruptedException e) {
                LOGGER.error("interrupted_exception:", e);
            }
        } else if (page.getUrl().regex("https://item.jd.com/[0-9]+.html").match()) {
            // detail page
            doWithDetailPage(page);

        } else {
            doWithListPage(page);
        }
    }

    /**
     * 处理列表页
     * @param page page对象
     */
    private void doWithListPage(Page page) {
        putSkuIdsToPage(page);
        int pageSize = grabProductDetails(page);

        String url = page.getUrl().get();
        String keyword = getKeyword(url);
        int pageNum = getPageNum(page, keyword);
        addUrls(page, keyword, pageNum, pageSize);
    }

    /**
     * 处理详情页
     * @param page page对象
     */
    private void doWithDetailPage(Page page) {
        Document document = page.getHtml().getDocument();
        Elements classes = document.getElementsByClass("crumb fl clearfix");
        if (classes != null && classes.size() == 0) {
            return;
        }
        Element itemClass = classes.get(0);
        if (itemClass == null) {
            return;
        }

        Elements itemClasses = itemClass.getElementsByClass("item");
        StringBuilder builder = new StringBuilder();
        if (itemClasses != null) {
            for (Element item : itemClasses) {
                Elements a = item.getElementsByTag("a");
                if (a == null || a.size() == 0) {
                    continue;
                }
                builder.append(a.get(0).text()).append("-");
            }
        }

        page.putField("skuId", page.getUrl().get()
                .replace(".html", "").replace("https://item.jd.com/", ""));
        String productClass = builder.toString();
        productClass = productClass.substring(0, productClass.length() - 1);
        page.putField(PageItemKeys.JD_DETAIL_PAGE.getKey(), productClass);
    }

    /**
     * 处理评论页
     * @param page page对象
     * @throws InterruptedException exception
     */
    private void doWithCommentPage(Page page) throws InterruptedException {
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
        if (rawText.equals("<html><head></head><body></body></html>")) {
            Thread.sleep(TimeUnit.SECONDS.toMillis(150L));
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

        JsonPathSelector nicknameSelector = new JsonPathSelector("$.comments[*].nickname");
        List<String> nicknames = nicknameSelector.selectList(text);
        minSize = Math.min(minSize, nicknames.size());

        JsonPathSelector plusSelector = new JsonPathSelector("$.comments[*].plusAvailable");
        List<String> plus = plusSelector.selectList(text);
        minSize = Math.min(minSize, plus.size());

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
            comment.setNickname(nicknames.get(i));
            comment.setProductType(productSizes.get(i));
            comment.setStar(Integer.parseInt(scores.get(i)));
            comment.setTime(creationTimes.get(i));
            comment.setPlus("201".equals(plus.get(i)));
            commentList.add(comment);
        }

        page.putField("commentList", commentList);
    }

    /**
     * 从列表页抓取商品的skuId
     * @param page
     */
    private void putSkuIdsToPage(Page page) {
        // get skuIds
        List<String> detailUrls = page.getHtml().xpath("//*[@id=\"J_goodsList\"]/ul/li/div[1]/div[1]/a/@href").all();
        List<String> skuIds = new ArrayList<>();
        for (String url : detailUrls) {
            System.out.println(url);
            String skuId = url.replace(".html", "").replace("//item.jd.com/", "");
            skuIds.add(skuId);
        }
        page.putField("skuIds", skuIds);
    }

    /**
     * 从列表页获取商品信息
     * @param page page对象
     * @return pageSize，页面上一共有多少个商品
     */
    private int grabProductDetails(Page page) {
        // get product detail
        Document document = page.getHtml().getDocument();
        int minSize = Integer.MAX_VALUE;
        Elements titles = document.getElementsByClass("p-name p-name-type-2");
        minSize = Math.min(minSize, titles.size());

        Elements prices = document.getElementsByClass("p-price");
        minSize = Math.min(minSize, prices.size());

        List<JDModel> modelList = new ArrayList<>();
        Elements commits = document.getElementsByClass("p-commit");
        minSize = Math.min(minSize, commits.size());

        Elements shops = document.getElementsByClass("p-shop");
        minSize = Math.min(minSize, shops.size());

        Elements icons = document.getElementsByClass("p-icons");
        minSize = Math.min(minSize, icons.size());

        List<String> skuIds = page.getResultItems().get("skuIds");
        minSize = Math.min(minSize, skuIds.size());

        try {
            for (int i = 0; i < minSize; i++) {
                JDModel jdModel = new JDModel();
                // get title
                jdModel.setTitle(titles.get(i).getElementsByTag("em").get(0).text());

                // get price
                String price = prices.get(i).getElementsByTag("i").get(0).text();
                if (price == null) {
                    LOGGER.warn("jd_spider_processor_excption:price is null");
                    continue;
                }
                jdModel.setPrice(price);

                // get sellCount
                int s = commits.get(i).getElementsByTag("a").size();
                String commit = (s > 1) ? commits.get(i).getElementsByTag("a").get(1).text() :
                        commits.get(i).getElementsByTag("a").get(0).text();
                jdModel.setSellCount(commit);

                // get shop
                if (shops.get(i).getElementsByTag("a").size() > 0) {
                    jdModel.setShop(shops.get(i).getElementsByTag("a").get(0).text());
                }

                // get icons
                Elements tempIcons = icons.get(i).getElementsByTag("i");
                StringBuilder builder = new StringBuilder();
                for (Element tempIcon : tempIcons) {
                    builder.append(tempIcon.text()).append(" ");
                }
                jdModel.setIcon(builder.toString());

                // get keyword
                String url = page.getUrl().get();
                jdModel.setKeyword(getKeyword(url));

                // get skuId
                jdModel.setSkuId(skuIds.get(i));

                modelList.add(jdModel);
            }
        } catch (Exception e) {
            LOGGER.error("================grab product details error:" + e + "====================");
            modelList = new ArrayList<>();
            minSize = 0;
        }

        page.putField(PageItemKeys.JD_LIST_PAGE.getKey(), modelList);

        return minSize;
    }

    /**
     * 从详情页的url获取keyword
     * @param url url
     * @return keyword
     */
    private String getKeyword(String url) {
        String keyword = null;
        try {
            String deUrl = URLDecoder.decode(url, "utf-8");
            keyword = UrlUtil.getFromUrl(deUrl, "keyword");

        } catch (UnsupportedEncodingException e) {
            LOGGER.error("unsupported_encoding_when_decoding_url:", e);
            keyword = UrlUtil.getFromUrl(url, "keyword");
        }
        return keyword;
    }

    /**
     * 列表页时，向爬取队列中添加详情页url以及评论url
     * @param page page对象
     */
    private void addUrls(Page page, String keyword, int pageNum, int pageSize) {
        List<String> detailUrls = getDetailUrls(page);
        List<String> commentUrls = getCommentUrls(page);
        String nextUrl = getNextPageRequest(page, keyword, pageNum, pageSize);

        List<String> urls = new ArrayList<>();
        for (int i = 0; i < detailUrls.size(); i++) {
            urls.add(detailUrls.get(i));
            for (int j = i * 3; j < i * 3 + 3; j++) {
                urls.add(commentUrls.get(j));
            }
        }
        if (nextUrl != null) {
            urls.add(nextUrl);
        }
        page.addTargetRequests(urls);
    }

    /**
     * 添加详情页url
     * @param page page对象
     * @return 详情页url列表
     */
    private List<String> getDetailUrls(Page page) {
        return page.getHtml().xpath("//*[@id=\"J_goodsList\"]/ul/li/div[1]/div[1]/a/@href").all();
    }

    /**
     * 添加评论url
     * @param page page对象
     * @return 评论url列表
     */
    private List<String> getCommentUrls(Page page) {
        List<String> skuIds = page.getResultItems().get("skuIds");
        if (CollectionUtils.isEmpty(skuIds)) {
            return new ArrayList<>();
        }

        List<String> commentUrls = new ArrayList<>();
        // add comment links
        for (String skuId : skuIds) {
            List<String> commentTargets = getCommentRequests(skuId);
            if (CollectionUtils.isNotEmpty(commentTargets)) {
                commentUrls.addAll(commentTargets);
            }
        }
        return commentUrls;
    }

    /**
     * 处于列表页时，获取一共有多少页数
     * @param page page对象
     * @return 总共的页数
     */
    private int getPageNum(Page page, String keyword) {
        SpiderAdvance advance;
        if (keyword != null && (advance = SpiderAdvanceCache.get(keyword)) != null) {
            if (advance.getPageNum() > 0) {
                return advance.getPageNum();
            }
        }

        Document document = page.getHtml().getDocument();
        Element bottom = document.getElementById("J_bottomPage");
        if (bottom == null) {
            return DEFAULT_PAGE_NUM;
        }

        Elements skip = bottom.getElementsByClass("p-skip");
        if (skip == null || skip.size() == 0) {
            return DEFAULT_PAGE_NUM;
        }

        Element em = skip.get(0).getElementsByTag("em").get(0);
        String pageNumStr = em.getElementsByTag("b").get(0).text();
        return Integer.parseInt(pageNumStr);
    }

    /**
     * 处于列表页时，获取下一页的url
     * @param page page对象
     * @param pageNum 共有多少页
     * @param pageSize 本页有多少商品
     * @return url
     */
    private String getNextPageRequest(Page page, String keyword, int pageNum, int pageSize) {
        // get next page
        int maxPage = pageNum * 2 - 1;
        Selectable rawUrl = page.getUrl();
        String url = rawUrl.get();
        Map<String, String> params = UrlUtil.getUrlParams(url);
        assert params != null;
        int tempPage = Integer.parseInt(params.get("page"));
        int nextPage = tempPage + 2;
        if (nextPage > maxPage) {
            SpiderAdvanceCache.remove(keyword);
            return null;
        }
        Integer nextStart = Integer.parseInt(params.get("s")) + pageSize;
        url = UrlUtil.addParamToUrl(url, "page", String.valueOf(nextPage));
        url = UrlUtil.addParamToUrl(url, "s", String.valueOf(nextStart));

        setAdvance(keyword, pageNum, tempPage);

        return url;
    }

    private void setAdvance(String keyword, int pageNum, int tempPage) {
        // show to front page
        SpiderAdvance advance;
        if ((advance = SpiderAdvanceCache.get(keyword)) == null) {
            advance = new SpiderAdvance();
            advance.setKeyword(keyword);
        }
        advance.setPageNum(pageNum);
        advance.setTemp((tempPage + 1) / 2);
        SpiderAdvanceCache.put(keyword, advance);
    }

    /**
     * 根据单个商品的skuId获取该商品的评论
     * 包括好评、中评和差评
     * @param skuId skuId
     * @return 单个商品的三个层级评论url
     */
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
