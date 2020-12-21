package com.ecspider.common.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lyifee
 * on 2020/12/21
 */
public class WebDriverPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverPool.class);

    private int CAPACITY = 5;

    private AtomicInteger refCount = new AtomicInteger(0);

    private BlockingQueue<WebDriver> driverQueue;

    private static DesiredCapabilities caps;

    private static String DRIVER_PATH;

    private static final ReentrantLock lock = new ReentrantLock();

    private static final Integer PAGELOAD_TIMEOUT_SECONDS = 60;

    private static final String DRIVER_PHANTOMJS = "phantomjs";

    static {
        caps.setJavascriptEnabled(true);
        caps.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, DRIVER_PATH);
        caps.setCapability("takesScreenshot", true);
        caps.setCapability(
                PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX
                        + "User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
                "--load-images=no");
    }

    public WebDriverPool(int capacity) {
        CAPACITY = capacity;
        driverQueue = new LinkedBlockingQueue<WebDriver>(capacity);

    }

    public WebDriver get() throws InterruptedException {
        WebDriver driver = driverQueue.poll();
        if (driver != null) {
            return driver;
        }

        if (refCount.get() < CAPACITY) {
            lock.lock();
            try {
                if (refCount.get() < CAPACITY) {
                    WebDriver newDriver = new PhantomJSDriver();
                    newDriver.manage().timeouts()
                            .pageLoadTimeout(PAGELOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    driverQueue.add(newDriver);
                    refCount.incrementAndGet();
                }
            } catch (Exception e) {
                LOGGER.error("get_new_webDriver_failed : ", e);

            } finally {
                lock.unlock();
            }
        }

        return driverQueue.take();
    }

    public void returnToPool(WebDriver webDriver) {
        if (!driverQueue.contains(webDriver)) {
            driverQueue.add(webDriver);
        }
    }

    public void close(WebDriver webDriver) {
        webDriver.close();
        webDriver.quit();
        driverQueue.remove(webDriver);
        refCount.decrementAndGet();
    }

    public void shutdown() {
        try {
            for (WebDriver driver : driverQueue) {
                driver.close();
                driver.quit();
            }
            driverQueue.clear();

        } catch (Exception e) {
            LOGGER.error("closing_webDriverPool_failed:", e);
        }
    }
}
