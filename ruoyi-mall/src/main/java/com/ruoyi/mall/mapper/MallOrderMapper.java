package com.ruoyi.mall.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.domain.MallOrder;

public interface MallOrderMapper
{
    MallOrder selectMallOrderById(Long orderId);

    List<MallOrder> selectMallOrderList(MallOrder order);

    int insertMallOrder(MallOrder order);

    int updateMallOrder(MallOrder order);

    int deleteMallOrderById(Long orderId);

    int deleteMallOrderByIds(Long[] orderIds);

    List<MallOrder> selectMallOrderListByUserId(@Param("userId") Long userId, @Param("status") String status);
}
