package com.ecspider.common;

import com.ecspider.common.util.ConfigUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.proxy.Proxy;

import java.io.IOException;

/**
 * @author lyifee
 * on 2021/1/10
 */
@Component
public class ProxyPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyPool.class);

    @Autowired
    private RequestSender requestSender;

    private final String proxyPoolSite = "101.37.89.200";

    private final String proxyPoolPort = "5000";

    public ProxyPool() {
        // proxyPoolSite = ConfigUtil.getValueToString("application.yml", "props.proxypool.site");
        // proxyPoolPort = ConfigUtil.getValueToString("application.yml", "props.proxypool.port");
    }

    public Proxy getProxy() {
        String url = proxyPoolSite + ":" + proxyPoolPort + "/get/";
        Request request = new Request(url);
        HttpUriRequest uriRequest = requestSender.getHttpUriRequest(request);
        try {
            CloseableHttpResponse response = requestSender.request(uriRequest);

        } catch (IOException e) {
            LOGGER.error("io_exception_getting_proxy:", e);
        }
        return null;
    }

}
