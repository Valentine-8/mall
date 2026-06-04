package com.ruoyi.mall.service;

import java.util.List;
import com.ruoyi.mall.domain.vo.MallCartVo;

public interface IMallCartService
{
    List<MallCartVo> selectCartList(Long userId);

    int addToCart(Long userId, Long productId, Integer quantity);

    int updateQuantity(Long userId, Long cartId, Integer quantity);

    int updateChecked(Long userId, Long cartId, String checked);

    int removeCart(Long userId, Long[] cartIds);
}
