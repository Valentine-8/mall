package com.ruoyi.web.controller.mall;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mall.domain.MallOrder;
import com.ruoyi.mall.service.IMallOrderService;

@RestController
@RequestMapping("/mall/order")
public class MallOrderController extends BaseController
{
    @Autowired
    private IMallOrderService orderService;

    @PreAuthorize("@ss.hasPermi('mall:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(MallOrder order)
    {
        startPage();
        List<MallOrder> list = orderService.selectMallOrderList(order);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:order:query')")
    @GetMapping("/{orderId}")
    public AjaxResult getInfo(@PathVariable Long orderId)
    {
        return success(orderService.selectMallOrderById(orderId));
    }

    @PreAuthorize("@ss.hasPermi('mall:order:ship')")
    @Log(title = "订单发货", businessType = BusinessType.UPDATE)
    @PutMapping("/ship/{orderId}")
    public AjaxResult ship(@PathVariable Long orderId)
    {
        return toAjax(orderService.shipOrder(orderId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mall:order:finish')")
    @Log(title = "订单完成", businessType = BusinessType.UPDATE)
    @PutMapping("/finish/{orderId}")
    public AjaxResult finish(@PathVariable Long orderId)
    {
        return toAjax(orderService.finishOrder(orderId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mall:order:cancel')")
    @Log(title = "订单取消", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{orderId}")
    public AjaxResult cancel(@PathVariable Long orderId)
    {
        return toAjax(orderService.cancelOrder(orderId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mall:order:cancel')")
    @Log(title = "订单退款", businessType = BusinessType.UPDATE)
    @PutMapping("/refund/{orderId}")
    public AjaxResult refund(@PathVariable Long orderId)
    {
        return toAjax(orderService.refundOrder(orderId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mall:order:remove')")
    @Log(title = "订单删除", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(orderService.deleteMallOrderByIds(orderIds));
    }
}
