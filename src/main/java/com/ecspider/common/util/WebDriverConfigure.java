package com.ecspider.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author lyifee
 * on 2020/12/22
 */
@Configuration
@Component(value = "webDriverConfigure")
public class WebDriverConfigure {
    @Value("${props.driver.path}")
    private String driverPath;

    public String getDriverPath() {
        return driverPath;
    }
}
