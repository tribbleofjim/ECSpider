package com.ecspider.common.downloader;

import com.ecspider.common.model.SeleniumAction;
import com.ecspider.common.util.WebDriverPool;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.selector.PlainText;
import javax.annotation.Resource;
import java.util.Map;

/**
 * @author lyifee
 * on 2020/12/21
 */
public class SeleniumDownloader implements Downloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumDownloader.class);

    private static final Long SLEEP_MILLIS = 4000L;

    private WebDriverPool webDriverPool = new WebDriverPool();

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
            Thread.sleep(SLEEP_MILLIS);
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

        manager.window().maximize();

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
}
