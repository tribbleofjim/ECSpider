package com.ecspider.common.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * @author lyifee
 * on 2020/12/23
 */
@Document(collection = "myJDdata")
public class JDModel {
    @Field("keyword")
    private String keyword;

    @Field("skuId")
    private String skuId;

    @Field("title")
    private String title;

    @Field("price")
    private String price;

    @Field("shop")
    private String shop;

    @Field("sellCount")
    private String sellCount;

    @Field("icon")
    private String icon;

    @Field("productClass")
    private String productClass;

    @Field("commentList")
    private List<JDComment> commentList;

    @Override
    public String toString() {
        return "JDModel{" +
                "keyword='" + keyword + '\'' +
                ", skuId='" + skuId + '\'' +
                ", title='" + title + '\'' +
                ", price='" + price + '\'' +
                ", shop='" + shop + '\'' +
                ", sellCount='" + sellCount + '\'' +
                ", icon='" + icon + '\'' +
                ", productClass='" + productClass + '\'' +
                ", commentList=" + commentList +
                '}';
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public String getProductClass() {
        return productClass;
    }

    public void setProductClass(String productClass) {
        this.productClass = productClass;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public String getSellCount() {
        return sellCount;
    }

    public void setSellCount(String sellCount) {
        this.sellCount = sellCount;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<JDComment> getCommentList() {
        return commentList;
    }

    public void setCommentList(List<JDComment> commentList) {
        this.commentList = commentList;
    }
}
