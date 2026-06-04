package com.ruoyi.mall.service;

import java.util.List;
import com.ruoyi.mall.domain.MallOrder;
import com.ruoyi.mall.domain.dto.MallCheckoutDto;

public interface IMallAppOrderService
{
    MallOrder createOrderFromCart(Long userId, String userName, MallCheckoutDto checkout);

    int payOrder(Long userId, Long orderId);

    List<MallOrder> selectMyOrderList(Long userId, String status);

    MallOrder selectMyOrderDetail(Long userId, Long orderId);

    int cancelMyOrder(Long userId, Long orderId);

    int confirmReceive(Long userId, Long orderId);
}
