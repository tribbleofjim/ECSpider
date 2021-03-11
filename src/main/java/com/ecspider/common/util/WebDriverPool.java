package com.ecspider.common.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webmaple.common.util.ConfigUtil;

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
    // TODO :  目前只支持Chrome，以后扩展到支持所有的webDriver
    private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverPool.class);

    private int capacity = 5;

    private AtomicInteger refCount = new AtomicInteger(0);

    private BlockingQueue<WebDriver> driverQueue;

    private static ChromeOptions options = new ChromeOptions();

    private static final ReentrantLock lock = new ReentrantLock();

    private static final Integer PAGELOAD_TIMEOUT_SECONDS = 60;

    public WebDriverPool() {
        init();
    }

    public WebDriverPool(int capacity) {
        this.capacity = capacity;
        init();
    }

    public void init() {
        driverQueue = new LinkedBlockingQueue<>(capacity);
        System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY,
                ConfigUtil.getValueToString("application.yml", "props.driver.path"));
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("blink-settings=imagesEnabled=false");
        options.addArguments("--disable-gpu");
        options.addArguments("--headless");
    }

    public WebDriver get() throws InterruptedException {
        WebDriver driver = driverQueue.poll();
        if (driver != null) {
            LOGGER.info("---get_webDriver---");
            return driver;
        }

        if (refCount.get() < capacity) {
            lock.lock();
            try {
                if (refCount.get() < capacity) {
                    WebDriver newDriver = new ChromeDriver(options);
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
        LOGGER.info("---start_return_to_pool---");
        if (!driverQueue.contains(webDriver)) {
            LOGGER.info("---return_to_pool---");
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
