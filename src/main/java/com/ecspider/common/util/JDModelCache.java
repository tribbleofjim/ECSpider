package com.ecspider.common.util;

import com.ecspider.common.model.JDComment;
import com.ecspider.common.model.JDModel;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lyifee
 * on 2020/12/24
 */
public class JDModelCache {
    public static final Integer CACHE_CAPACITY = 30;

    private static final Map<String, JDModel> modelMap = new ConcurrentHashMap<>();

    private static final List<JDModel> modelList = new ArrayList<>();

    public static void add(String skuId, JDModel model) {
        if (StringUtils.isBlank(skuId)) {
            return;
        }
        modelMap.put(skuId, model);
    }

    public static void addComments(String skuId, List<JDComment> comments, boolean isEnd) {
        if (StringUtils.isBlank(skuId)) {
            return;
        }
        JDModel jdModel = modelMap.getOrDefault(skuId, null);
        if (jdModel == null) {
            return;
        }
        if (jdModel.getCommentList() == null) {
            jdModel.setCommentList(comments);

        } else {
            jdModel.getCommentList().addAll(comments);
        }
        if (isEnd) {
            modelList.add(jdModel);
            modelMap.remove(skuId);
        }
    }

    public static int getTempSize() {
        return modelList.size();
    }

    public static List<JDModel> getModelList() {
        return modelList;
    }

    public static void clear() {
        modelList.clear();
    }
}
