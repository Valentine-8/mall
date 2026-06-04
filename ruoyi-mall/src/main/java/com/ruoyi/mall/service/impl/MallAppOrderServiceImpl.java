package com.ruoyi.mall.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.domain.MallOrder;
import com.ruoyi.mall.domain.MallOrderItem;
import com.ruoyi.mall.domain.dto.MallCheckoutDto;
import com.ruoyi.mall.domain.vo.MallCartVo;
import com.ruoyi.mall.mapper.MallCartMapper;
import com.ruoyi.mall.mapper.MallOrderItemMapper;
import com.ruoyi.mall.mapper.MallOrderMapper;
import com.ruoyi.mall.mapper.MallProductMapper;
import com.ruoyi.mall.service.IMallAppOrderService;
import com.ruoyi.mall.service.IMallCartService;

@Service
public class MallAppOrderServiceImpl implements IMallAppOrderService
{
    private static final String MSG_NO_SETTLE = "请先勾选要结算的商品";
    private static final String MSG_OFF = "商品已下架：";
    private static final String MSG_LOW = "库存不足：";
    private static final String MSG_DEDUCT = "扣减库存失败：";
    private static final String MSG_MISSING = "订单不存在";
    private static final String MSG_FORBID = "无权操作该订单";
    private static final String MSG_DONE = "订单已完成或已取消";
    private static final String MSG_CANCEL_RULE = "当前状态不允许取消";
    private static final String MSG_PAY_ONLY = "仅待付款订单可支付";
    private static final String MSG_RECEIVE = "仅已发货订单可确认收货";

    @Autowired
    private MallOrderMapper orderMapper;
    @Autowired
    private MallOrderItemMapper orderItemMapper;
    @Autowired
    private MallProductMapper productMapper;
    @Autowired
    private MallCartMapper cartMapper;
    @Autowired
    private IMallCartService cartService;

    @Override
    @Transactional
    public MallOrder createOrderFromCart(Long userId, String userName, MallCheckoutDto checkout)
    {
        List<MallCartVo> settleList = filterSettleCarts(cartService.selectCartList(userId), checkout.getCartIds());
        if (settleList.isEmpty()) throw new ServiceException(MSG_NO_SETTLE);
        BigDecimal total = BigDecimal.ZERO;
        List<MallOrderItem> items = new ArrayList<>();
        List<Long> cartIdsToRemove = new ArrayList<>();
        for (MallCartVo cart : settleList)
        {
            if (!"0".equals(cart.getStatus())) throw new ServiceException(MSG_OFF + cart.getProductName());
            if (cart.getStock() < cart.getQuantity()) throw new ServiceException(MSG_LOW + cart.getProductName());
            BigDecimal lineTotal = cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            total = total.add(lineTotal);
            MallOrderItem item = new MallOrderItem();
            item.setProductId(cart.getProductId());
            item.setProductName(cart.getProductName());
            item.setProductPic(cart.getPic());
            item.setProductPrice(cart.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setTotalAmount(lineTotal);
            items.add(item);
            cartIdsToRemove.add(cart.getCartId());
        }
        MallOrder order = new MallOrder();
        order.setOrderSn(generateOrderSn());
        order.setUserId(userId);
        order.setUserName(userName);
        order.setTotalAmount(total);
        order.setPayAmount(total);
        order.setStatus("0");
        order.setReceiverName(checkout.getReceiverName());
        order.setReceiverPhone(checkout.getReceiverPhone());
        order.setReceiverAddress(checkout.getReceiverAddress());
        order.setRemark(checkout.getRemark());
        order.setCreateBy(userName);
        orderMapper.insertMallOrder(order);
        for (MallOrderItem item : items)
        {
            item.setOrderId(order.getOrderId());
            if (productMapper.decreaseStock(item.getProductId(), item.getQuantity()) == 0)
                throw new ServiceException(MSG_DEDUCT + item.getProductName());
            productMapper.increaseSaleCount(item.getProductId(), item.getQuantity());
        }
        orderItemMapper.batchInsertMallOrderItem(items);
        cartMapper.deleteCartByIds(userId, cartIdsToRemove.toArray(new Long[0]));
        order.setItems(items);
        return order;
    }

    @Override
    public int payOrder(Long userId, Long orderId) {
        MallOrder order = requireOwnPendingOrder(userId, orderId);
        MallOrder update = new MallOrder();
        update.setOrderId(orderId);
        update.setStatus("1");
        update.setPayTime(new Date());
        update.setUpdateBy(order.getUserName());
        return orderMapper.updateMallOrder(update);
    }

    @Override
    public List<MallOrder> selectMyOrderList(Long userId, String status) {
        return orderMapper.selectMallOrderListByUserId(userId, status);
    }

    @Override
    public MallOrder selectMyOrderDetail(Long userId, Long orderId) {
        MallOrder order = orderMapper.selectMallOrderById(orderId);
        if (order == null || !userId.equals(order.getUserId())) throw new ServiceException(MSG_MISSING);
        order.setItems(orderItemMapper.selectMallOrderItemByOrderId(orderId));
        return order;
    }

    @Override
    public int cancelMyOrder(Long userId, Long orderId) {
        MallOrder order = orderMapper.selectMallOrderById(orderId);
        if (order == null || !userId.equals(order.getUserId())) throw new ServiceException(MSG_MISSING);
        if ("3".equals(order.getStatus()) || "4".equals(order.getStatus()) || "5".equals(order.getStatus()))
            throw new ServiceException(MSG_DONE);
        if ("0".equals(order.getStatus())) {
            for (MallOrderItem item : orderItemMapper.selectMallOrderItemByOrderId(orderId))
                productMapper.restoreStock(item.getProductId(), item.getQuantity());
            MallOrder update = new MallOrder();
            update.setOrderId(orderId);
            update.setStatus("4");
            update.setCancelTime(new Date());
            return orderMapper.updateMallOrder(update);
        }
        throw new ServiceException(MSG_CANCEL_RULE);
    }

    @Override
    public int confirmReceive(Long userId, Long orderId) {
        MallOrder order = orderMapper.selectMallOrderById(orderId);
        if (order == null || !userId.equals(order.getUserId())) throw new ServiceException(MSG_MISSING);
        if (!"2".equals(order.getStatus())) throw new ServiceException(MSG_RECEIVE);
        MallOrder update = new MallOrder();
        update.setOrderId(orderId);
        update.setStatus("3");
        update.setFinishTime(new Date());
        update.setUpdateBy(order.getUserName());
        return orderMapper.updateMallOrder(update);
    }

    private List<MallCartVo> filterSettleCarts(List<MallCartVo> allCarts, List<Long> cartIds) {
        List<MallCartVo> result = new ArrayList<>();
        for (MallCartVo cart : allCarts) {
            boolean selected = cartIds != null && !cartIds.isEmpty()
                ? cartIds.contains(cart.getCartId()) : "1".equals(cart.getChecked());
            if (selected) result.add(cart);
        }
        return result;
    }

    private MallOrder requireOwnPendingOrder(Long userId, Long orderId) {
        MallOrder order = orderMapper.selectMallOrderById(orderId);
        if (order == null || !userId.equals(order.getUserId())) throw new ServiceException(MSG_MISSING);
        if (!"0".equals(order.getStatus())) throw new ServiceException(MSG_PAY_ONLY);
        return order;
    }

    private String generateOrderSn() {
        return "MO" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
