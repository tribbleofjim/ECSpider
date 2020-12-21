package com.ecspider.common.util;

import org.openqa.selenium.WebDriver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class WebDriverPool {
    private int CAPACITY = 5;

    private AtomicInteger refCount = new AtomicInteger(0);

    private static final String DRIVER_PHANTOMJS = "phantomjs";

    private BlockingQueue<WebDriver> driverQueue = new LinkedBlockingQueue<WebDriver>(CAPACITY);
}
