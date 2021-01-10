package com.ecspider.common;

import com.ecspider.common.util.RequestUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.utils.CharsetUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author lyifee
 * on 2021/1/10
 */
@Component
public class RequestSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestSender.class);

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    public HttpUriRequest getHttpUriRequest(Request request) {
        return RequestUtil.convertHttpUriRequest(request, null, null);
    }

    public CloseableHttpResponse request(HttpUriRequest request) throws IOException {
        return httpClient.execute(request);
    }


}
