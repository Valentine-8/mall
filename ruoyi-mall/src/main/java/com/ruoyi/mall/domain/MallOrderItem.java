package com.ruoyi.mall.domain;

import java.math.BigDecimal;

/**
 * 订单明细 mall_order_item
 */
public class MallOrderItem
{
    private Long itemId;
    private Long orderId;
    private Long productId;
    private String productName;
    private String productPic;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal totalAmount;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getProductId()
    {
        return productId;
    }

    public void setProductId(Long productId)
    {
        this.productId = productId;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public String getProductPic()
    {
        return productPic;
    }

    public void setProductPic(String productPic)
    {
        this.productPic = productPic;
    }

    public BigDecimal getProductPrice()
    {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice)
    {
        this.productPrice = productPrice;
    }

    public Integer getQuantity()
    {
        return quantity;
    }

    public void setQuantity(Integer quantity)
    {
        this.quantity = quantity;
    }

    public BigDecimal getTotalAmount()
    {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount)
    {
        this.totalAmount = totalAmount;
    }
}
