package com.ecspider.common.pipeline;

import com.ecspider.common.enums.PageItemKeys;
import com.ecspider.common.model.JDComment;
import com.ecspider.common.model.JDModel;
import com.ecspider.common.util.JDModelCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;
import java.util.Map;

/**
 * @author lyifee
 * on 2020/12/21
 */
@Component
public class JDPipeline implements Pipeline {
    Logger LOGGER = LoggerFactory.getLogger(JDPipeline.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    public void process(ResultItems resultItems, Task task) {
        Map<String, Object> mapResults = resultItems.getAll();
        if (mapResults == null || mapResults.isEmpty()) {
            return;
        }

        List<JDModel> modelList;
        List<String> skuIds;
        if ((modelList = resultItems.get(PageItemKeys.JD_PAGE_KEY.getKey())) != null) {
            // list page
            if ((skuIds = resultItems.get("skuIds")) == null) {
                return;
            }

            int size = Math.min(modelList.size(), skuIds.size());
            for (int i = 0; i < size; i++) {
                JDModelCache.add(skuIds.get(i), modelList.get(i));
            }

        } else {
            List<JDComment> commentList;
            if ((commentList = resultItems.get("commentList")) == null) {
                return;
            }
            String skuId;
            if ((skuId = resultItems.get("skuId")) == null) {
                return;
            }

            if (Integer.parseInt(resultItems.get("score")) == 3) {
                JDModelCache.addComments(skuId, commentList);
            }
        }

        if (JDModelCache.getTempSize() >= JDModelCache.CACHE_CAPACITY) {
            mongoTemplate.insert(JDModelCache.getModelList(), JDModel.class);
            JDModelCache.clear();
        }

    }
}
