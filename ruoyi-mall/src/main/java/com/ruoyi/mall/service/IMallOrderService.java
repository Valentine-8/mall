package com.ruoyi.mall.service;

import java.util.List;
import com.ruoyi.mall.domain.MallOrder;

public interface IMallOrderService
{
    MallOrder selectMallOrderById(Long orderId);

    List<MallOrder> selectMallOrderList(MallOrder order);

    int shipOrder(Long orderId, String updateBy);

    int finishOrder(Long orderId, String updateBy);

    int cancelOrder(Long orderId, String updateBy);

    int refundOrder(Long orderId, String updateBy);

    int deleteMallOrderByIds(Long[] orderIds);
}
