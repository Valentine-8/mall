package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.domain.MallOrder;
import com.ruoyi.mall.domain.dto.MallCheckoutDto;
import com.ruoyi.mall.service.IMallAppOrderService;

/**
 * C-end order (login required)
 */
@RestController
@RequestMapping("/app/mall/order")
public class AppMallOrderController extends BaseController
{
    @Autowired
    private IMallAppOrderService appOrderService;

    @PostMapping("/checkout")
    public AjaxResult checkout(@Validated @RequestBody MallCheckoutDto checkout)
    {
        MallOrder order = appOrderService.createOrderFromCart(getUserId(), getUsername(), checkout);
        return success(order);
    }

    @PutMapping("/pay/{orderId}")
    public AjaxResult pay(@PathVariable Long orderId)
    {
        return toAjax(appOrderService.payOrder(getUserId(), orderId));
    }

    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String status)
    {
        return success(appOrderService.selectMyOrderList(getUserId(), status));
    }

    @GetMapping("/{orderId}")
    public AjaxResult detail(@PathVariable Long orderId)
    {
        return success(appOrderService.selectMyOrderDetail(getUserId(), orderId));
    }

    @PutMapping("/cancel/{orderId}")
    public AjaxResult cancel(@PathVariable Long orderId)
    {
        return toAjax(appOrderService.cancelMyOrder(getUserId(), orderId));
    }

    @PutMapping("/confirm/{orderId}")
    public AjaxResult confirmReceive(@PathVariable Long orderId)
    {
        return toAjax(appOrderService.confirmReceive(getUserId(), orderId));
    }
}
