package com.ecspider.common.model;

/**
 * @author lyifee
 * on 2020/12/25
 */
public class JDComment {
    /**
     * 评价的星级，1/2/3/4/5
     */
    private Integer star;

    /**
     * 评价的文本内容
     */
    private String content;

    /**
     * 商品类型
     */
    private String productType;

    /**
     * 评价时间
     */
    private String time;

    @Override
    public String toString() {
        return "JDComment{" +
                "star=" + star +
                ", content='" + content + '\'' +
                ", productType='" + productType + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

    public Integer getStar() {
        return star;
    }

    public void setStar(Integer star) {
        this.star = star;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
