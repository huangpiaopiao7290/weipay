package com.weipay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.weipay.entity.OrderInfo;
import com.weipay.entity.Product;
import com.weipay.enums.OrderStatus;
import com.weipay.mapper.OrderInfoMapper;
import com.weipay.mapper.ProductMapper;
import com.weipay.service.OrderInfoService;
import com.weipay.utils.OrderNoUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 订单信息接口实现类
 */
@Service("OrderInfoServiceImpl")
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper,OrderInfo> implements OrderInfoService {

    @Resource(name = "ProductMapper")
    private ProductMapper productMapper;

    @Resource(name = "OrderInfoMapper")
    private OrderInfoMapper orderInfoMapper;

    /**
     * 查找已存在但未支付的订单
     * @param productId 商品ID
     * @return OrderInfo 订单信息
     */
    private OrderInfo getNoPayOrderByProductId(Long productId) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        queryWrapper.eq("order_status",OrderStatus.NOTPAY);
        // 实际情况还需要判断是哪个用户的订单
        // queryWrapper.eq("user_id", userId);
        // 因为表中有code_url字段，而code_url的有效期只有2h，超过这个时间再下单返回的二维码就是无效的，需要与create_time进行判断
        // 这里不做处理，后面有定时任务，定期更新保证存储的二维码都是可用的
        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapper);


        return orderInfo;
    }
    /**
     * 根据商品ID创建订单
     * @param productId 商品ID
     * @return OrderInfo 订单信息
     */
    @Override
    public OrderInfo createOrderByProductId(Long productId) {

        // 查找已存在该商品的订单但还没支付
        OrderInfo noPayOrderByProductId = this.getNoPayOrderByProductId(productId);
        if (noPayOrderByProductId != null) {
            return noPayOrderByProductId;
        }
        // 获取商品信息
        Product product = productMapper.selectById(productId);
        // 生成订单
        OrderInfo order = new OrderInfo();
        order.setTitle(product.getTitle());                     // 订单名称
        order.setOrderNo(OrderNoUtil.getOrderNo());             // 订单编号
        order.setProductId(productId);                          // 订单商品编号
        order.setTotalFee(product.getPrice());                  // 订单金额
        order.setOrderStatus(OrderStatus.NOTPAY.getType());     // 订单状态
        // 将订单写入订单表中
        orderInfoMapper.insert(order);

        return order;
    }

    /**
     * 存储订单二维码
     * @param orderNo 订单编号
     * @param codeUrl 要用二维码展示的url
     */
    @Override
    public void saveCodeUrl(String orderNo, String codeUrl) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no",orderNo);

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCodeUrl(codeUrl);

        orderInfoMapper.update(orderInfo,queryWrapper);
    }

    /**
     * 查询订单列表,根据创建时间展示，最近的展示到前面
     *
     *
     * 记得分页
     *
     *
     * @return List
     */
    @Override
    public List<OrderInfo> listOrderByCreateTimeDesc() {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");

        return orderInfoMapper.selectList(queryWrapper);
    }

    /**
     * 更新订单的状态
     * @param orderNo 订单号
     * @param orderStatus
     */
    @Override
    public void updateStatusByOrderNo(String orderNo, OrderStatus orderStatus) {
        // log.info("更新订单状态 ===> {}", orderStatus.getType());
        System.out.println("更新订单状态:=====>" + orderNo);
        // 当用户扫码完成并成功支付，将status更新成支付成功
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderStatus(orderStatus.getType());

        orderInfoMapper.update(orderInfo, queryWrapper);

    }

    /**
     *  查询订单状态
     * @param orderNo 订单编号
     * @return
     */
    @Override
    public String getOrderStatus(String orderNo) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapper);
        // 当订单不存在时
        if (orderInfo == null) {
            return null;
        }

        return orderInfo.getOrderStatus();
    }

    /**
     * 查询从创建时间开始间隔时间超过指定时间未支付的订单
     * @param minutes 时间间隔 单位 min
     * @return
     */
    @Override
    public List<OrderInfo> getNoPayOrderByDuration(int minutes) {
        // 系统minutes分钟之前
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime before = now.minusMinutes(minutes);
        // 定义格式
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String scheduleTime = dateTimeFormatter.format(before);
        System.out.println("schedule_time:" + scheduleTime);

        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        // 查询条件  未支付 且 超过minutes 分钟的订单
        queryWrapper.eq("order_status", OrderStatus.NOTPAY.getType());
        // schedule_time：当前时间减去5min, 查询create_time < schedule_time的订单
        queryWrapper.le("create_time", scheduleTime);

        List<OrderInfo> orderInfoList = orderInfoMapper.selectList(queryWrapper);

        return orderInfoList;
    }

    @Override
    public OrderInfo getOrderByOrderNo(String orderNo) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        OrderInfo orderInfo = baseMapper.selectOne(queryWrapper);
        return orderInfo;
    }
}
