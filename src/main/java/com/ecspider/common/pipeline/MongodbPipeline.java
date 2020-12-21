package com.ecspider.common.pipeline;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author lyifee
 * on 2020/12/21
 */
public class MongodbPipeline implements Pipeline {
    Logger LOGGER = LoggerFactory.getLogger(MongodbPipeline.class);

    @Resource
    private MongoTemplate mongoTemplate;

    public void process(ResultItems resultItems, Task task) {
        Map<String, Object> mapResults = resultItems.getAll();
        if (mapResults == null || mapResults.isEmpty()) {
            return;
        }

        Document document = new Document();
        for (Map.Entry<String, Object> entry : mapResults.entrySet()) {
            document.put(entry.getKey(), entry.getValue());
        }
        LOGGER.info(document.toString());
        mongoTemplate.insert(document);
    }
}
