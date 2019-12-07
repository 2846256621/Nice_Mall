package com.fdj.nicemallbackend.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fdj.nicemallbackend.common.utils.IdWorker;
import com.fdj.nicemallbackend.system.dto.Result;
import com.fdj.nicemallbackend.system.dto.StorageUpdate;
import com.fdj.nicemallbackend.system.dto.orderDto;
import com.fdj.nicemallbackend.system.entity.*;
import com.fdj.nicemallbackend.system.mapper.*;
import com.fdj.nicemallbackend.system.service.IBusinessService;
import com.fdj.nicemallbackend.system.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.swing.text.html.parser.Entity;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author xns
 * @since 2019-10-20
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    OrderStatusMapper orderStatusMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    StoreGoodsMapper storeGoodsMapper;

    @Autowired
    BusinessMapper businessMapper;

    /**
     * 创建订单
     *
     * @param order
     * @return
     */
    @Override
    @Transactional
    public Result createOrder(Order order) {
        IdWorker idWorker = new IdWorker();
        User user = userMapper.selectByuserId(order.getUserId());
        String orderId = String.valueOf(idWorker.nextId());
        order.setOrderId(orderId);
        order.setUserName(user.getUserName());
        LocalDateTime localDateTime = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        order.setCreateTime(localDateTime);
        orderMapper.save(order);

        //保存订单状态
        OrderStatus orderStatus = new OrderStatus(orderId, 0, order.getCreateTime());
        orderStatusMapper.save(orderStatus);


        List<StorageUpdate> lists = new ArrayList<>();
        for (OrderDetail orderDetail : order.getPayData()) {
            orderDetail.setOrderId(orderId);
            StorageUpdate storageUpdate = new StorageUpdate();
            storageUpdate.setGoodsId(orderDetail.getGoodsId());
            storageUpdate.setGoodsNum(orderDetail.getGoodsNum());
            lists.add(storageUpdate);
        }
        //批量插入订单详情
        orderDetailMapper.insertList(order.getPayData());
        //更新库存
        int isjuge = storeGoodsMapper.decreaseStock(lists);
        if (isjuge > 0) {
            return new Result().success(String.valueOf(orderId), "下单成功");
        } else {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new Result().fail("库存不足，无法下单");
        }
    }

    /**
     * 设置要更新的状态对应的时间
     *
     * @param orderId
     * @param orderStatus
     * @return
     */
    public OrderStatus setStatus(String orderId, Integer orderStatus) {
        OrderStatus orderStatus1 = new OrderStatus();
        orderStatus1.setOrderId(orderId);
        LocalDateTime localDateTime = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        switch (orderStatus) {
            case 1:
                orderStatus1.setPaymentTime(localDateTime);
                break;
            case 2:
                orderStatus1.setDeliveryTime(localDateTime);
                break;
            case 3:
                orderStatus1.setEndTime(localDateTime);
                break;
            default:
                log.error("状态不存在!");
                break;
        }
        orderStatus1.setOrderStatus(orderStatus);
        return orderStatus1;
    }

    /**
     * 更新订单状态
     *
     * @param orderId
     * @param orderStatus
     * @return
     */
    @Override
    public String updateOrderStatus(String orderId, Integer orderStatus) {
        OrderStatus orderStatus1 = setStatus(orderId, orderStatus);
        if (orderStatusMapper.updateByOrderId(orderStatus1) > 0) {
            return orderId;
        } else {
            log.error("更新出错");
            return null;
        }
    }

    /**
     * 商家查询订单
     *
     * @return
     */
    @Override
    public List<orderDto> getOneStatusOrders(Long userId, Integer orderStatus) {
        List<orderDto> lists = new ArrayList<>();
        Business business = businessMapper.selectByuserId(userId);
        if (business == null) {
            return lists;
        }
        lists = orderDetailMapper.selectOneStatusOrder(business.getStoreName(), orderStatus);
        return lists;
    }

    /**
     * 批量更新状态
     *
     * @param orderId
     * @param orderStatus
     * @return
     */
    @Override
    public String updateListOrderStatus(List<String> orderId, Integer orderStatus) {
        if (orderStatusMapper.updateByOrderIdList(orderId, orderStatus) > 0) {
            return "操作成功";
        } else {
            log.error("批量更新出错");
            return null;
        }
    }

    /**
     * 商家显示发货和未发货订单
     *
     * @param userId
     * @return
     */
    @Override
    public List<orderDto> getAllStatusOrders(Long userId) {
        List<orderDto> lists = new ArrayList<>();
        Business business = businessMapper.selectByuserId(userId);
        if (business == null) {
            return lists;
        }
        lists = orderDetailMapper.selectPartStatusOrder(business.getStoreName());
        for (int i = 0; i < lists.size(); i++) {
            switch (lists.get(i).getOrderStatus()) {
                case 1:
                    lists.get(i).setShip(false);
                    break;
                case 2:
                    lists.get(i).setShip(true);
                    break;
                default:
                    log.error("状态有误，不能识别");
                    break;
            }
        }
        return lists;
    }

    /**
     * 用户查询某一状态的订单
     *
     * @return
     */
    @Override
    public List<orderDto> queryOneStatus(Long userId, Integer orderStatus) {
        List<orderDto> lists = orderDetailMapper.OneStatusOrderByuserId(userId, orderStatus);
        String statusStr = null;
        switch (orderStatus) {
            case 0:
                statusStr = "待付款";
                break;
            case 1:
                statusStr = "代发货";
                break;
            case 2:
                statusStr = "待收货";
                break;
            case 3:
                statusStr = "待评价";
                break;
            case 4:
                statusStr = "已评价";
                break;
            default:
                log.error("状态有误，不能识别");
                break;
        }
        for (int i = 0; i < lists.size(); i++) {
            lists.get(i).setStatusStr(statusStr);
        }
        return lists;
    }

    /**
     * 用户查询所有的订单
     *
     * @return
     */
    @Override
    public List<orderDto> queryAllStatus(Long userId) {
        List<orderDto> lists = orderDetailMapper.allStatusOrderByuserId(userId);
        String statusStr = null;
        for (int i = 0; i < lists.size(); i++) {
            switch (lists.get(i).getOrderStatus()) {
                case 0:
                    lists.get(i).setStatusStr("待付款");
                    break;
                case 1:
                    lists.get(i).setStatusStr("代发货");
                    break;
                case 2:
                    lists.get(i).setStatusStr("待收货");
                    break;
                case 3:
                    lists.get(i).setStatusStr("待评价");
                    break;
                case 4:
                    lists.get(i).setStatusStr("已评价");
                    break;
                default:
                    log.error("状态有误，不能识别");
                    break;
            }
        }
        return lists;
    }

    /**
     * 用户获取订单详情
     *
     * @param orderId
     * @return
     */
    @Override
    public Order getOrderDetail(String orderId) {
        Order order = null;
        order = orderMapper.selectByOrderId(orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderId(orderId);
        OrderStatus orderStatus = orderStatusMapper.selectByOrderId(orderId);
        order.setPayData(orderDetails);
        order.setOrderStatus(orderStatus.getOrderStatus());
        return order;
    }

    /**
     * 清除过期未支付订单并且释放锁定的库存
     *
     * @param time
     */

    @Transactional
    @Override
    public void clearOverDueOrders(Integer time) {
        List<OrderStatus> list = orderStatusMapper.selectUnPay();
        /**
         * 将超时未支付订单清除
         */
        for (OrderStatus orderStatus : list) {
            List<StorageUpdate> lists = new ArrayList<>();
            if (orderStatus.getCreateTime().plusMinutes(time).isBefore(LocalDateTime.now())) {
                List<OrderDetail> list2 = orderDetailMapper.selectByOrderId(orderStatus.getOrderId());
                for (OrderDetail orderDetail : list2) {
                    StorageUpdate storageUpdate = new StorageUpdate();
                    storageUpdate.setGoodsId(orderDetail.getGoodsId());
                    storageUpdate.setGoodsNum(orderDetail.getGoodsNum());
                    lists.add(storageUpdate);
                }
                /**
                 * 取消锁定的库存
                 */
                int isjuge = storeGoodsMapper.unlockStock(lists);
                if (isjuge > 0) {
                    orderStatusMapper.deleteByOrderId(orderStatus.getOrderId());
                    orderDetailMapper.deleteByOrderId(orderStatus.getOrderId());
                    orderMapper.deleteByOrderId(orderStatus.getOrderId());
                    log.info(lists + "解锁成功");
                } else {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    log.error("解锁库存失败");
                }
            }
        }
    }
}

