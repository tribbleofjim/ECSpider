package com.ecspider.common.pipeline;

import com.ecspider.common.enums.PageItemKeys;
import com.ecspider.common.model.JDComment;
import com.ecspider.common.model.JDModel;
import com.ecspider.common.util.JDModelCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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
        String productClass;
        List<String> skuIds;

        if ((modelList = resultItems.get(PageItemKeys.JD_LIST_PAGE.getKey())) != null) {
            // list page
            if ((skuIds = resultItems.get("skuIds")) == null) {
                return;
            }

            int size = Math.min(modelList.size(), skuIds.size());
            for (int i = 0; i < size; i++) {
                JDModelCache.put(skuIds.get(i), modelList.get(i));
            }

        } else if ((productClass = resultItems.get(PageItemKeys.JD_DETAIL_PAGE.getKey())) != null) {
            String skuId = resultItems.get("skuId");
            if (StringUtils.isBlank(skuId)) {
                return;
            }
            JDModel model = JDModelCache.get(skuId);
            if (model == null) {
                return;
            }
            model.setProductClass(productClass);
            JDModelCache.put(skuId, model);

        } else {
            List<JDComment> commentList;
            if ((commentList = resultItems.get("commentList")) == null) {
                return;
            }
            String skuId;
            if ((skuId = resultItems.get("skuId")) == null) {
                return;
            }
            JDModelCache.addComments(skuId, commentList, Integer.parseInt(resultItems.get("score")) == 3);
        }

        if (JDModelCache.getTempSize() >= JDModelCache.CACHE_CAPACITY) {
            mongoTemplate.insert(JDModelCache.getModelList(), JDModel.class);
            JDModelCache.clear();
        }

    }
}
