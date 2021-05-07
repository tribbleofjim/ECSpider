package com.ecspider.web.service;

import org.springframework.stereotype.Service;
import org.webmaple.common.model.SpiderAdvance;
import org.webmaple.worker.SpiderAdvanceCache;

/**
 * @author lyifee
 * on 2020/12/27
 */
@Service
public class CommonService {
    public SpiderAdvance getAdvance(String keyword) {
        return SpiderAdvanceCache.get(keyword);
    }
}
