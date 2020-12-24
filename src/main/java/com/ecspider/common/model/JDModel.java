package com.ecspider.common.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * @author lyifee
 * on 2020/12/23
 */
@Document(collection = "jdData")
public class JDModel {
    @Field("title")
    private String title;

    @Field("price")
    private String price;

    @Field("shop")
    private String shop;

    @Field("sellCount")
    private String sellCount;

    @Field("commentList")
    private List<String> commentList;

    @Override
    public String toString() {
        return "JDModel{" +
                "title='" + title + '\'' +
                ", price='" + price + '\'' +
                ", shop='" + shop + '\'' +
                ", sellCount='" + sellCount + '\'' +
                ", commentList=" + commentList +
                '}';
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

    public List<String> getCommentList() {
        return commentList;
    }

    public void setCommentList(List<String> commentList) {
        this.commentList = commentList;
    }
}
