package com.weipay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weipay.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 订单表接口
 */
@Mapper
@Repository("OrderInfoMapper")
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

}
