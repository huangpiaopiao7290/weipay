package com.weipay.controller;

import com.weipay.entity.OrderInfo;
import com.weipay.enums.OrderStatus;
import com.weipay.service.OrderInfoService;
import com.weipay.vo.R;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 查询订单接口
 */
@CrossOrigin
@RestController
@RequestMapping("/api/order-info")
public class OrderInfoController {

    @Resource(name = "OrderInfoServiceImpl")
    private OrderInfoService orderInfoService;

    /**
     * 查询订单状态
     * @param orderNo 订单编号
     * @return R order_status
     */
    @GetMapping("/order-status/{orderNo}")
    public R getOrderStatus(@PathVariable String orderNo) {
         String orderStatus = orderInfoService.getOrderStatus(orderNo);
         // 判断订单状态
        if (OrderStatus.SUCCESS.getType().equals(orderStatus)) {
            return R.ok().setMessage("支付成功");
        }
        // 支付中...状态码 3500
        return R.ok().setCode(3500).setMessage("支付中...");
    }

    /**
     * 订单列表
     * @return R
     */
    @GetMapping("/list")
    public R getOrderInfoList() {
        // 按时间倒序
        List<OrderInfo> orderInfos = orderInfoService.listOrderByCreateTimeDesc();

        return R.ok().data("orders", orderInfos);
    }
}
