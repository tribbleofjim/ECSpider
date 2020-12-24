package com.ecspider.common.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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

    @Field("commit")
    private String commit;

    @Field("shop")
    private String shop;

    @Field("icon")
    private String icon;

    @Override
    public String toString() {
        return "JDModel{" +
                "title='" + title + '\'' +
                ", price='" + price + '\'' +
                ", commit='" + commit + '\'' +
                ", shop='" + shop + '\'' +
                ", icon='" + icon + '\'' +
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

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
