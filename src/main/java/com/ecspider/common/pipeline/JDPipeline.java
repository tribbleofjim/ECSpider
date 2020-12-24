package com.ecspider.common.pipeline;

import com.ecspider.common.model.JDModel;
import com.ecspider.common.util.JDModelCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import java.util.Map;

/**
 * @author lyifee
 * on 2020/12/21
 */
@Component
public class JDPipeline implements Pipeline {
    Logger LOGGER = LoggerFactory.getLogger(JDPipeline.class);

    private static final Integer CACHE_CAPACITY = 30;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void process(ResultItems resultItems, Task task) {
        Map<String, Object> mapResults = resultItems.getAll();
        if (mapResults == null || mapResults.isEmpty()) {
            return;
        }

        JDModel model = new JDModel();
        model.setTitle(resultItems.get("title"));
        model.setPrice(resultItems.get("price"));
        model.setShop(resultItems.get("shop"));
        model.setSellCount(resultItems.get("sellCount"));
        model.setCommentList(resultItems.get("commentList"));
        JDModelCache.add(model);
        if (JDModelCache.getModelList().size() >= CACHE_CAPACITY) {
            mongoTemplate.insert(JDModelCache.getModelList(), JDModel.class);
            JDModelCache.clear();
        }

    }
}
