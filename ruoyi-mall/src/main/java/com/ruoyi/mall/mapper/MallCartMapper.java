package com.ruoyi.mall.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.domain.MallCart;
import com.ruoyi.mall.domain.vo.MallCartVo;

public interface MallCartMapper
{
    List<MallCartVo> selectCartVoListByUserId(Long userId);

    MallCart selectCartByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    MallCart selectCartById(@Param("cartId") Long cartId);

    int insertMallCart(MallCart cart);

    int updateMallCart(MallCart cart);

    int deleteCartById(@Param("cartId") Long cartId);

    int deleteCartByIds(@Param("userId") Long userId, @Param("cartIds") Long[] cartIds);

    int deleteCartByUserId(Long userId);
}
