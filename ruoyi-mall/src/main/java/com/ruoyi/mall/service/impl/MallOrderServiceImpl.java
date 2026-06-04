package com.ruoyi.mall.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.domain.MallOrder;
import com.ruoyi.mall.domain.MallOrderItem;
import com.ruoyi.mall.mapper.MallOrderItemMapper;
import com.ruoyi.mall.mapper.MallOrderMapper;
import com.ruoyi.mall.mapper.MallProductMapper;
import com.ruoyi.mall.service.IMallOrderService;

@Service
public class MallOrderServiceImpl implements IMallOrderService
{
    private static final String MSG_SHIP = "仅已付款订单可发货";
    private static final String MSG_FINISH = "仅已发货订单可完成";
    private static final String MSG_CANCEL = "订单已完成或已取消";
    private static final String MSG_REFUND = "仅已付款或已发货订单可退款";
    private static final String MSG_MISSING = "订单不存在";

    @Autowired
    private MallOrderMapper orderMapper;
    @Autowired
    private MallOrderItemMapper orderItemMapper;
    @Autowired
    private MallProductMapper productMapper;

    @Override
    public MallOrder selectMallOrderById(Long orderId)
    {
        MallOrder order = orderMapper.selectMallOrderById(orderId);
        if (order != null)
            order.setItems(orderItemMapper.selectMallOrderItemByOrderId(orderId));
        return order;
    }

    @Override
    public List<MallOrder> selectMallOrderList(MallOrder order)
    {
        return orderMapper.selectMallOrderList(order);
    }

    @Override
    public int shipOrder(Long orderId, String updateBy)
    {
        MallOrder order = requireOrder(orderId);
        if (!"1".equals(order.getStatus())) throw new ServiceException(MSG_SHIP);
        MallOrder update = new MallOrder();
        update.setOrderId(orderId);
        update.setStatus("2");
        update.setDeliveryTime(new Date());
        update.setUpdateBy(updateBy);
        return orderMapper.updateMallOrder(update);
    }

    @Override
    public int finishOrder(Long orderId, String updateBy)
    {
        MallOrder order = requireOrder(orderId);
        if (!"2".equals(order.getStatus())) throw new ServiceException(MSG_FINISH);
        MallOrder update = new MallOrder();
        update.setOrderId(orderId);
        update.setStatus("3");
        update.setFinishTime(new Date());
        update.setUpdateBy(updateBy);
        return orderMapper.updateMallOrder(update);
    }

    @Override
    public int cancelOrder(Long orderId, String updateBy)
    {
        MallOrder order = requireOrder(orderId);
        if ("3".equals(order.getStatus()) || "4".equals(order.getStatus()) || "5".equals(order.getStatus()))
            throw new ServiceException(MSG_CANCEL);
        MallOrder update = new MallOrder();
        update.setOrderId(orderId);
        update.setStatus("4");
        update.setCancelTime(new Date());
        update.setUpdateBy(updateBy);
        return orderMapper.updateMallOrder(update);
    }

    @Override
    @Transactional
    public int refundOrder(Long orderId, String updateBy)
    {
        MallOrder order = requireOrder(orderId);
        if (!"1".equals(order.getStatus()) && !"2".equals(order.getStatus()))
            throw new ServiceException(MSG_REFUND);
        for (MallOrderItem item : orderItemMapper.selectMallOrderItemByOrderId(orderId))
            productMapper.restoreStock(item.getProductId(), item.getQuantity());
        MallOrder update = new MallOrder();
        update.setOrderId(orderId);
        update.setStatus("5");
        update.setCancelTime(new Date());
        update.setUpdateBy(updateBy);
        return orderMapper.updateMallOrder(update);
    }

    @Override
    @Transactional
    public int deleteMallOrderByIds(Long[] orderIds)
    {
        orderItemMapper.deleteMallOrderItemByOrderIds(orderIds);
        return orderMapper.deleteMallOrderByIds(orderIds);
    }

    private MallOrder requireOrder(Long orderId)
    {
        MallOrder order = orderMapper.selectMallOrderById(orderId);
        if (order == null) throw new ServiceException(MSG_MISSING);
        return order;
    }
}
