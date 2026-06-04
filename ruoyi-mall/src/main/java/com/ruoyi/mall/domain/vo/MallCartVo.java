package com.ruoyi.mall.domain.vo;

import java.math.BigDecimal;

/**
 * Cart line with product snapshot for C-end display
 */
public class MallCartVo
{
    private Long cartId;
    private Long productId;
    private String productName;
    private String pic;
    private BigDecimal price;
    private Integer stock;
    private String status;
    private Integer quantity;
    private String checked;

    public Long getCartId()
    {
        return cartId;
    }

    public void setCartId(Long cartId)
    {
        this.cartId = cartId;
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

    public String getPic()
    {
        return pic;
    }

    public void setPic(String pic)
    {
        this.pic = pic;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public Integer getStock()
    {
        return stock;
    }

    public void setStock(Integer stock)
    {
        this.stock = stock;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getQuantity()
    {
        return quantity;
    }

    public void setQuantity(Integer quantity)
    {
        this.quantity = quantity;
    }

    public String getChecked()
    {
        return checked;
    }

    public void setChecked(String checked)
    {
        this.checked = checked;
    }
}
