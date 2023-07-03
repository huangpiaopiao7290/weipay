package com.weipay.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 商品表的实体类
 */
@TableName("t_product")
public class Product extends BaseEntity{

    private String title;           //商品名称
    private Integer price;          //价格（分）

    public Product() {
    }

    public Product(String id, Date createTime, Date updateTime, String title, Integer price) {
        super(id, createTime, updateTime);
        this.title = title;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return super.toString() + "Product{" +
                "title='" + title + '\'' +
                ", price=" + price +
                '}';
    }
}
