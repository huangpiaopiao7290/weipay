package com.weipay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.weipay.entity.Product;


public interface ProductService extends IService<Product> {

    /**
     * 根据订单id查询商品信息
     * @param OrderId
     * @return
     */
    Product queryProductAndSendInfo(String OrderId);

}
