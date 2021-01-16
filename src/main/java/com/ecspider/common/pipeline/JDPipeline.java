package com.ecspider.common.pipeline;

import com.ecspider.common.enums.PageItemKeys;
import com.ecspider.common.model.JDComment;
import com.ecspider.common.model.JDModel;
import com.ecspider.common.util.ConfigUtil;
import com.ecspider.common.util.JDModelCache;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lyifee
 * on 2020/12/21
 */
public class JDPipeline implements Pipeline {
    Logger LOGGER = LoggerFactory.getLogger(JDPipeline.class);

    private final MongoClient mongoClient;

    public JDPipeline() {
        String username = ConfigUtil.getValueToString("application.yml", "spring.data.mongodb.username");
        String database = ConfigUtil.getValueToString("application.yml", "spring.data.mongodb.database");
        String password = ConfigUtil.getValueToString("application.yml", "spring.data.mongodb.password");
        List<ServerAddress> adds = new ArrayList<>();
        //ServerAddress()两个参数分别为 服务器地址 和 端口
        ServerAddress serverAddress = new ServerAddress("localhost", 27017);
        adds.add(serverAddress);

        List<MongoCredential> credentials = new ArrayList<>();
        //MongoCredential.createScramSha1Credential()三个参数分别为 用户名 数据库名称 密码
        MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(username, database, password.toCharArray());
        credentials.add(mongoCredential);

        //通过连接认证获取MongoDB连接
        mongoClient = new MongoClient(adds, credentials);
    }

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
            insertMongo();
            JDModelCache.clear();
        }

    }

    private void insertMongo() {
        MongoDatabase database = mongoClient.getDatabase("data");
        MongoCollection<Document> collection = database.getCollection("jdData");
        List<Document> list = new ArrayList<>();
        List<JDModel> modelList = JDModelCache.getModelList();
        for (JDModel model : modelList) {
            Document document = new Document("keyword", model.getKeyword())
                    .append("skuId", model.getSkuId())
                    .append("title", model.getTitle())
                    .append("price", model.getPrice())
                    .append("shop", model.getShop())
                    .append("sellCount", model.getSellCount())
                    .append("icon", model.getIcon())
                    .append("productClass", model.getProductClass())
                    ;
            List<JDComment> commentList = model.getCommentList();
            List<Document> commentDocs = new ArrayList<>();
            for (JDComment comment : commentList) {
                Document commentDoc = new Document("star", comment.getStar())
                        .append("nickname", comment.getNickname())
                        .append("isPlus", comment.getPlus())
                        .append("content", comment.getContent())
                        .append("productType", comment.getProductType())
                        .append("time", comment.getTime())
                        ;
                commentDocs.add(commentDoc);
            }
            document.append("commentList", commentDocs);
            list.add(document);
        }
        collection.insertMany(list);
    }
}
