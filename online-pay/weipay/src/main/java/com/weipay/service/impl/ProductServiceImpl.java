package com.weipay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.weipay.entity.OrderInfo;
import com.weipay.entity.Product;
import com.weipay.mapper.OrderInfoMapper;
import com.weipay.mapper.ProductMapper;
import com.weipay.service.ProductService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("ProductServiceImpl")
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Resource(name = "ProductMapper")
    private ProductMapper productMapper;

    @Resource(name = "OrderInfoMapper")
    private OrderInfoMapper orderInfoMapper;

    @Override
    public Product queryProductAndSendInfo(String orderId) {
        // 根据订单号查询商品id
        QueryWrapper<OrderInfo> productIdQueryWrapper = new QueryWrapper<>();
        productIdQueryWrapper.eq("order_no", orderId);
        OrderInfo orderInfo = orderInfoMapper.selectOne(productIdQueryWrapper);

        Long productId = orderInfo.getProductId();


        // 查询商品信息
        return productMapper.selectById(productId);
    }
}
