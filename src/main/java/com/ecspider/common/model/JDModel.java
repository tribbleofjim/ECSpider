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

    @Override
    public String toString() {
        return "JDModel{" +
                "title='" + title + '\'' +
                ", price='" + price + '\'' +
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
}
