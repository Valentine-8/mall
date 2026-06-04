package com.ruoyi.mall.domain.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Checkout request from C-end
 */
public class MallCheckoutDto
{
    @NotBlank(message = "receiver name required")
    private String receiverName;

    @NotBlank(message = "receiver phone required")
    private String receiverPhone;

    @NotBlank(message = "receiver address required")
    private String receiverAddress;

    /** Cart ids to settle; empty means all checked items */
    private List<Long> cartIds;

    private String remark;

    public String getReceiverName()
    {
        return receiverName;
    }

    public void setReceiverName(String receiverName)
    {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone()
    {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone)
    {
        this.receiverPhone = receiverPhone;
    }

    public String getReceiverAddress()
    {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress)
    {
        this.receiverAddress = receiverAddress;
    }

    public List<Long> getCartIds()
    {
        return cartIds;
    }

    public void setCartIds(List<Long> cartIds)
    {
        this.cartIds = cartIds;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
