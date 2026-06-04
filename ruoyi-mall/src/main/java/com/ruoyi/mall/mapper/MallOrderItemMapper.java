package com.ruoyi.mall.mapper;

import java.util.List;
import com.ruoyi.mall.domain.MallOrderItem;

public interface MallOrderItemMapper
{
    List<MallOrderItem> selectMallOrderItemByOrderId(Long orderId);

    int batchInsertMallOrderItem(List<MallOrderItem> items);

    int deleteMallOrderItemByOrderId(Long orderId);

    int deleteMallOrderItemByOrderIds(Long[] orderIds);
}
