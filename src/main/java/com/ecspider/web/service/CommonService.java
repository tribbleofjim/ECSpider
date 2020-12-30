package com.ecspider.web.service;

import com.ecspider.common.SpiderAdvanceCache;
import com.ecspider.web.model.SpiderAdvance;
import org.springframework.stereotype.Service;

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
