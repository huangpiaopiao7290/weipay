package com.weipay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weipay.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 商品表接口
 */
@Mapper
@Repository("ProductMapper")
public interface ProductMapper extends BaseMapper<Product> {

}
