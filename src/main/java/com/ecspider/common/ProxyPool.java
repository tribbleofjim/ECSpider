package com.ecspider.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ecspider.common.util.ConfigUtil;
import com.ecspider.common.util.RequestUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.proxy.Proxy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lyifee
 * on 2021/1/10
 */
@Component
public class ProxyPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyPool.class);

    @Autowired
    private RequestSender requestSender;

    private String proxyPoolSite = "127.0.0.1";

    private String proxyPoolPort = "8080";

    public ProxyPool() {
        try {
            proxyPoolSite = ConfigUtil.getValueToString("application.yml", "props.proxypool.site");
            proxyPoolPort = ConfigUtil.getValueToString("application.yml", "props.proxypool.port");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public Proxy getProxy() {
        String url = getUrl("get");
        Request request = new Request(url);
        HttpUriRequest uriRequest = requestSender.getHttpUriRequest(request);
        try {
            CloseableHttpResponse response = requestSender.request(uriRequest);
            String text = RequestUtil.getResponseText(response);
            JSONObject jsonObject = JSON.parseObject(text);
            String proxyString = (String) jsonObject.get("proxy");
            return getFromProxyString(proxyString);

        } catch (IOException e) {
            LOGGER.error("io_exception_getting_proxy:", e);
        }
        return null;
    }

    public List<Proxy> getProxyList() {
        String url = getUrl("get_all");
        Request request = new Request(url);
        HttpUriRequest uriRequest = requestSender.getHttpUriRequest(request);
        try {
            CloseableHttpResponse response = requestSender.request(uriRequest);
            String text = RequestUtil.getResponseText(response);
            JSONArray jsonArray = JSON.parseArray(text);
            List<Proxy> proxyList = new ArrayList<>();
            for (Object o : jsonArray) {
                JSONObject jsonObject = (JSONObject) o;
                String proxyString = (String) jsonObject.get("proxy");
                Proxy proxy = getFromProxyString(proxyString);
                if (proxy == null) {
                    continue;
                }
                proxyList.add(proxy);
            }
            return proxyList;

        } catch (IOException e) {
            LOGGER.error("io_exception_getting_proxy:", e);
        }
        return null;
    }

    private String getUrl(String method) {
        return proxyPoolSite + ":" + proxyPoolPort + "/" + method + "/";
    }

    private Proxy getFromProxyString(String proxyString) {
        String[] params = proxyString.split(":");
        if (params.length < 2) {
            LOGGER.error("invalid_proxy_format:{}", proxyString);
            return null;
        }
        String host = params[0];
        String port = params[1];
        return new Proxy(host, Integer.parseInt(port));
    }

    public String getProxyPoolSite() {
        return proxyPoolSite;
    }

    public void setProxyPoolSite(String proxyPoolSite) {
        this.proxyPoolSite = proxyPoolSite;
    }

    public String getProxyPoolPort() {
        return proxyPoolPort;
    }

    public void setProxyPoolPort(String proxyPoolPort) {
        this.proxyPoolPort = proxyPoolPort;
    }
}
