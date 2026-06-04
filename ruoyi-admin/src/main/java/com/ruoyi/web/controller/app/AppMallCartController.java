package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.ruoyi.mall.service.IMallCartService;

/**
 * C-end shopping cart (login required)
 */
@RestController
@RequestMapping("/app/mall/cart")
public class AppMallCartController extends BaseController
{
    @Autowired
    private IMallCartService cartService;

    @GetMapping("/list")
    public AjaxResult list()
    {
        return success(cartService.selectCartList(getUserId()));
    }

    @PostMapping("/add")
    public AjaxResult add(@RequestParam Long productId, @RequestParam(defaultValue = "1") Integer quantity)
    {
        return toAjax(cartService.addToCart(getUserId(), productId, quantity));
    }

    @PutMapping("/quantity")
    public AjaxResult updateQuantity(@RequestParam Long cartId, @RequestParam Integer quantity)
    {
        return toAjax(cartService.updateQuantity(getUserId(), cartId, quantity));
    }

    @PutMapping("/checked")
    public AjaxResult updateChecked(@RequestParam Long cartId, @RequestParam String checked)
    {
        return toAjax(cartService.updateChecked(getUserId(), cartId, checked));
    }

    @DeleteMapping("/{cartIds}")
    public AjaxResult remove(@PathVariable Long[] cartIds)
    {
        return toAjax(cartService.removeCart(getUserId(), cartIds));
    }
}
