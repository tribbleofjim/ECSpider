package com.ecspider.common.enums;

/**
 * @author lyifee
 * on 2020/12/23
 */
public enum PageItemKeys {
    JD_PAGE_KEY("jdModelList")
    ;
    private String key;

    PageItemKeys(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
