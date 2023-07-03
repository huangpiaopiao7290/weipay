package com.weipay.task;

import com.weipay.entity.OrderInfo;
import com.weipay.entity.RefundInfo;
import com.weipay.service.OrderInfoService;
import com.weipay.service.RefundInfoService;
import com.weipay.service.WxPayService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 定时查单
 */
@Component("WxPayTask")
public class WxPayTask {

    @Resource(name = "OrderInfoServiceImpl")
    private OrderInfoService orderInfoService;

    @Resource(name = "RefundInfoServiceImpl")
    private RefundInfoService refundInfoService;

    @Resource(name = "WxPayServiceImpl")
    private WxPayService wxPayService;

    /**
     * 每隔60s查询 超过创建时间5min的未支付订单的状态
     * 秒分时日月年
     * 每小时的第0分0秒开始，每5min触发一次
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void orderConfirm() throws Exception {
        System.out.println("定时任务启动，每5min查询一次超过5min未支付的订单");
        List<OrderInfo> orderInfoList = orderInfoService.getNoPayOrderByDuration(5);
        System.out.println("定时查询满足条件的订单列表：" + orderInfoList);
        if (orderInfoList != null && !orderInfoList.isEmpty()) {
            // 提取订单号
            for (OrderInfo orderInfo : orderInfoList) {
                String orderNo = orderInfo.getOrderNo();
                System.out.println("未支付且超过5min的订单号=" + orderNo);
                // 核实订单状态  调用微信查单接口
                wxPayService.checkOrderStatus(orderNo);
            }
        }
    }

    /**
     * 从第0秒开始每隔1min执行1次，查询创建超过1分钟，并且未成功的退款单
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void refundConfirm() throws Exception {
        System.out.println("定时任务启动，每30s查询一次超过1min未退款成功的订单记录 ");

        //找出申请退款超过5分钟并且未成功的退款单
        List<RefundInfo> refundInfoList = refundInfoService.getNoRefundOrderByDuration(1);
        System.out.println("定时查询满足条件的退款单列表：" + refundInfoList);
        if (!refundInfoList.isEmpty()) {
            for (RefundInfo refundInfo : refundInfoList) {
                String refundNo = refundInfo.getRefundNo();
                System.out.println("超时未退款的退款单号 ===> " +  refundNo);

                //核实订单状态：调用微信支付查询退款接口
                wxPayService.checkRefundStatus(refundNo);
            }
        }

    }

}
