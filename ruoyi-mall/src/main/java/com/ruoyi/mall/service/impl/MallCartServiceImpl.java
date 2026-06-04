package com.ruoyi.mall.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.domain.MallCart;
import com.ruoyi.mall.domain.MallProduct;
import com.ruoyi.mall.domain.vo.MallCartVo;
import com.ruoyi.mall.mapper.MallCartMapper;
import com.ruoyi.mall.mapper.MallProductMapper;
import com.ruoyi.mall.service.IMallCartService;

@Service
public class MallCartServiceImpl implements IMallCartService
{
    private static final String MSG_STOCK = "库存不足";
    private static final String MSG_QTY = "数量至少为1";
    private static final String MSG_CART = "购物车记录不存在";
    private static final String MSG_PRODUCT = "商品不存在或已下架";

    @Autowired
    private MallCartMapper cartMapper;
    @Autowired
    private MallProductMapper productMapper;

    @Override
    public List<MallCartVo> selectCartList(Long userId)
    {
        return cartMapper.selectCartVoListByUserId(userId);
    }

    @Override
    public int addToCart(Long userId, Long productId, Integer quantity)
    {
        MallProduct product = requireOnSaleProduct(productId);
        if (quantity == null || quantity < 1) quantity = 1;
        if (product.getStock() < quantity) throw new ServiceException(MSG_STOCK);
        MallCart exist = cartMapper.selectCartByUserAndProduct(userId, productId);
        if (exist != null)
        {
            int newQty = exist.getQuantity() + quantity;
            if (product.getStock() < newQty) throw new ServiceException(MSG_STOCK);
            exist.setQuantity(newQty);
            exist.setChecked("1");
            return cartMapper.updateMallCart(exist);
        }
        MallCart cart = new MallCart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(quantity);
        cart.setChecked("1");
        return cartMapper.insertMallCart(cart);
    }

    @Override
    public int updateQuantity(Long userId, Long cartId, Integer quantity)
    {
        MallCart cart = requireOwnCart(userId, cartId);
        MallProduct product = requireOnSaleProduct(cart.getProductId());
        if (quantity == null || quantity < 1) throw new ServiceException(MSG_QTY);
        if (product.getStock() < quantity) throw new ServiceException(MSG_STOCK);
        cart.setQuantity(quantity);
        return cartMapper.updateMallCart(cart);
    }

    @Override
    public int updateChecked(Long userId, Long cartId, String checked)
    {
        MallCart cart = requireOwnCart(userId, cartId);
        cart.setChecked("1".equals(checked) ? "1" : "0");
        return cartMapper.updateMallCart(cart);
    }

    @Override
    public int removeCart(Long userId, Long[] cartIds)
    {
        if (cartIds == null || cartIds.length == 0) return 0;
        return cartMapper.deleteCartByIds(userId, cartIds);
    }

    private MallCart requireOwnCart(Long userId, Long cartId)
    {
        MallCart cart = cartMapper.selectCartById(cartId);
        if (cart == null || !userId.equals(cart.getUserId()))
            throw new ServiceException(MSG_CART);
        return cart;
    }

    private MallProduct requireOnSaleProduct(Long productId)
    {
        MallProduct product = productMapper.selectMallProductById(productId);
        if (product == null || !"0".equals(product.getStatus()))
            throw new ServiceException(MSG_PRODUCT);
        return product;
    }
}
