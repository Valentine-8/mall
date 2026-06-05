package com.ruoyi.mall.service;

import java.util.List;
import com.ruoyi.mall.domain.vo.MallCartVo;

public interface IMallCartService
{
    List<MallCartVo> selectCartList(Long userId);

    int addToCart(Long userId, Long productId, Integer quantity);

    /** @param replaceQuantity true = set qty (buy now), false = add to existing qty */
    Long addToCartReturnId(Long userId, Long productId, Integer quantity, boolean replaceQuantity);

    int updateQuantity(Long userId, Long cartId, Integer quantity);

    int updateChecked(Long userId, Long cartId, String checked);

    int removeCart(Long userId, Long[] cartIds);
}
