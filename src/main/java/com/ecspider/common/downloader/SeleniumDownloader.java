package com.ecspider.common.downloader;

import com.ecspider.common.model.SeleniumAction;
import com.ecspider.common.util.WebDriverPool;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.selector.PlainText;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author lyifee
 * on 2020/12/21
 */
public class SeleniumDownloader implements Downloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumDownloader.class);

    private final WebDriverPool webDriverPool = new WebDriverPool();

    public Page download(Request request, Task task) {
        WebDriver webDriver;
        try {
           webDriver = webDriverPool.get();

        } catch (InterruptedException e) {
            LOGGER.error("get_webDriver_from_pool_failed : ", e);
            return null;
        }

        Page page = new Page();
        try {
            webDriver.get(request.getUrl());
            Thread.sleep(getSleepMillis());
        } catch (InterruptedException e) {
            LOGGER.error("webDriver_get_url_exception:", e);
        }

        WebDriver.Options manager = webDriver.manage();
        Site site = task.getSite();

        // add cookies
        if (manager.getCookies() != null) {
            for (Map.Entry<String, String> entry : site.getCookies().entrySet()) {
                Cookie cookie = new Cookie(entry.getKey(), entry.getValue());
                manager.addCookie(cookie);
            }
        }

        // page execution
        manager.window().maximize();
        // 将页面滚动到底部后休眠1秒，确保页面上的所有元素加载出来
        ((JavascriptExecutor) webDriver).executeScript("window.scrollTo(0,document.body.scrollHeight)");
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1L));

        } catch (InterruptedException e) {
            LOGGER.error("selenium_downloader_interrupted:", e);
        }

        // do action
        SeleniumAction reqAction=(SeleniumAction) request.getExtra("action");
        if (reqAction != null) {
            reqAction.execute(webDriver);
        }

        // grab html and store it into page
        WebElement webElement = webDriver.findElement(By.xpath("/html"));
        String content = webElement.getAttribute("outerHTML");
        page.setRawText(content);
        page.setUrl(new PlainText(webDriver.getCurrentUrl()));
        page.setRequest(request);
        webDriverPool.returnToPool(webDriver);
        return page;
    }

    public void setThread(int i) {

    }

    private int getSleepMillis() {
        Random random = new Random();
        return random.nextInt(7000) + 3000;
    }
}
