package com.ecspider.web.model;

/**
 * @author lyifee
 * on 2020/12/27
 */
public class SpiderAdvance {
    private String keyword;

    private int pageNum;

    private int temp;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }
}
