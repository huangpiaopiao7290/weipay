package com.weipay.service;

import com.weipay.entity.RefundInfo;

import java.util.List;

/**
 * 退款业务
 */
public interface RefundInfoService {

    /**
     * 生成退款订单
     * @param orderNo 订单号
     * @param reason 退款原因
     * @return
     */
    RefundInfo createRefundInfoByOrderNo (String orderNo, String reason);

    /**
     * 更新退款单  退款申请完成后补全退款单中的必要信息
     * @param content
     */
    void updateRefund(String content);

    /**
     * 找出申请退款 minutes分钟之前且未成功的退款记录
     * @param minutes 分钟数
     * @return
     */
    List<RefundInfo> getNoRefundOrderByDuration(int minutes);

}
