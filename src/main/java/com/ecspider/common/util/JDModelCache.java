package com.ecspider.common.util;

import com.ecspider.common.model.JDModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lyifee
 * on 2020/12/24
 */
public class JDModelCache {
    private static List<JDModel> modelList = new ArrayList<>();

    public static void add(JDModel model) {
        modelList.add(model);
    }

    public static List<JDModel> getModelList() {
        return modelList;
    }

    public static void clear() {
        modelList.clear();
    }
}
