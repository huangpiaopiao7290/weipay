package com.weipay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.gson.Gson;
import com.weipay.entity.OrderInfo;
import com.weipay.entity.RefundInfo;
import com.weipay.enums.wxpay.WxRefundStatus;
import com.weipay.mapper.RefundInfoMapper;
import com.weipay.service.OrderInfoService;
import com.weipay.service.RefundInfoService;
import com.weipay.utils.OrderNoUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 退款业务实现
 */
@Service("RefundInfoServiceImpl")
public class RefundInfoServiceImpl implements RefundInfoService {

    @Resource(name = "OrderInfoServiceImpl")
    private OrderInfoService orderInfoService;

    @Resource(name = "RefundInfoMapper")
    private RefundInfoMapper refundInfoMapper;

    /**
     * 生成退款订单
     * @param orderNo 订单号
     * @param reason 退款原因
     * @return
     */
    @Override
    public RefundInfo createRefundInfoByOrderNo(String orderNo, String reason) {
        // 获取待退款的订单信息
        OrderInfo toRefundOrder = orderInfoService.getOrderByOrderNo(orderNo);

        RefundInfo refundInfo = new RefundInfo();
        refundInfo.setOrderNo(orderNo);                                             // 订单编号
        refundInfo.setRefundNo(OrderNoUtil.getRefundNo());                          // 退款单编号
        refundInfo.setTotalFee(toRefundOrder.getTotalFee());                        // 原订单金额(分)
        refundInfo.setRefund(toRefundOrder.getTotalFee());                          // 退款金额(分)
        refundInfo.setReason(reason);                                               // 退款原因

        refundInfoMapper.insert(refundInfo);

        return refundInfo;
    }

    /**
     * 更新退款单  退款申请完成后补全退款单中的必要信息
     * @param content
     */
    @Override
    public void updateRefund(String content) {
        Gson gson = new Gson();
        // 将json响应数据转换为map
        Map<String, String> refundMap = gson.fromJson(content, HashMap.class);

        String refundId = refundMap.get("refund_id");              // 微信支付退款单号
        String refundNo = refundMap.get("out_refund_no");       // 退款的订单号
        String status = null;                                             // 退款状态

        System.out.println("微信支付退款单号=========》" + refundId + '\n' + "退款的订单号=======》 " + refundNo);

        System.out.println("updateRefund=========>传入的参数=====" + refundMap);
        RefundInfo refundInfo = new RefundInfo();
        refundInfo.setRefundId(refundId);
        if (((refundMap.get("status")) != null) && !(status = refundMap.get("status")).isEmpty()) {
            refundInfo.setRefundStatus(status);                     // 查询退款参数 和 申请退款参数——退款状态
            refundInfo.setContentReturn(content);                   // 申请退款和查询退款的所有返回参数存入退款表中
        }

        if (((refundMap.get("refund_status")) != null) && !(status = refundMap.get("refund_status")).isEmpty()) {
            refundInfo.setRefundStatus(status);                     // 退款通知回调的参数——退款状态
            refundInfo.setContentNotify(content);                   // 将退款通知回调的全部的响应结果存入退款表中
        }

        UpdateWrapper<RefundInfo> refundInfoUpdateWrapper = new UpdateWrapper<>();
        refundInfoUpdateWrapper.eq("refund_no", refundNo);
        // 更新退款表信息
        refundInfoMapper.update(refundInfo, refundInfoUpdateWrapper);
    }

    /**
     * 找出申请退款 minutes分钟之前且未成功的退款记录
     * @param minutes 分钟数
     * @return
     */
    @Override
    public List<RefundInfo> getNoRefundOrderByDuration(int minutes) {
        //minutes分钟之前的时间
        Instant instant = Instant.now().minus(Duration.ofMinutes(minutes));

        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("refund_status", WxRefundStatus.PROCESSING.getType());
        queryWrapper.le("create_time", instant);
        List<RefundInfo> refundInfoList = refundInfoMapper.selectList(queryWrapper);
        return refundInfoList;
    }
}
