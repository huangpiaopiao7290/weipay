package com.weipay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weipay.entity.PaymentInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 支付表接口
 */
@Mapper
@Repository("PaymentInfoMapper")
public interface PaymentInfoMapper extends BaseMapper<PaymentInfo> {
}
