package com.ecspider.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author lyifee
 * on 2020/12/22
 */
@Configuration
public class WebDriverConfigure {
    @Value("${props.driver.path}")
    private String driverPath;

    public String getDriverPath() {
        return driverPath;
    }

    public void setDriverPath(String driverPath) {
        this.driverPath = driverPath;
    }
}
