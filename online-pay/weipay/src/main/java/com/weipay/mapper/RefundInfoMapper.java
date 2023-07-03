package com.weipay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weipay.entity.RefundInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository("RefundInfoMapper")
public interface RefundInfoMapper extends BaseMapper<RefundInfo> {

}
