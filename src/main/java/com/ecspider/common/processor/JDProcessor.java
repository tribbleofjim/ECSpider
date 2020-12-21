package com.ecspider.common.processor;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * @author lyifee
 * on 2020/12/21
 */
public class JDProcessor implements PageProcessor {
    private static final Integer RETRY_TIMES = 3;

    private static final Integer SLEEP_TIME_MILLIS = 3000;

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) " +
            "AppleWebKit/537.31 (KHTML, like Gecko) " +
            "Chrome/26.0.1410.65 " +
            "Safari/537.31";

    private Site site = Site.me()
            .setRetryTimes(RETRY_TIMES)
            .setSleepTime(SLEEP_TIME_MILLIS)
            .setUserAgent(USER_AGENT);

    public void process(Page page) {

        // TODO : process products

        //正则匹配url是列表页还是详情列，列表页主要是获取一个页面中列表所有商品的详情页url，详情页为主要解析对象
//        if (!page.getUrl().regex("https://item.jd.com/[0-9]+.html").match()) {
//
//            //列表頁，addTargetRequests为新增在队列中，指url
//            page.addTargetRequests(page.getHtml().xpath("//*[@id=\"J_goodsList\"]/ul/li/div[1]/div[1]/a/@href").all());
//
//        } else {
//            //
//            page.putField("title",page.getHtml().xpath("//*[@class=\"sku-name\"]/text()").toString());
//            page.putField("price",page.getHtml().xpath("/html/body/div[6]/div/div[2]/div[3]/div/div[1]/div[2]/span[1]/span[2]/text()").toString());
//            page.putField("score",page.getHtml().xpath("//*[@id=\"comment-count\"]/a/text()").toString());
//            page.putField("content",page.getHtml().getDocument().getElementsByClass("p-parameter").text());
//            page.putField("url",page.getUrl().toString());
//        }

    }

    public Site getSite() {
        return site;
    }
}
