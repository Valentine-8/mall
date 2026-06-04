package com.ruoyi.web.controller.mall;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mall.domain.MallProduct;
import com.ruoyi.mall.service.IMallProductService;

@RestController
@RequestMapping("/mall/product")
public class MallProductController extends BaseController
{
    @Autowired
    private IMallProductService productService;

    @PreAuthorize("@ss.hasPermi('mall:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(MallProduct product)
    {
        startPage();
        List<MallProduct> list = productService.selectMallProductList(product);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:product:query')")
    @GetMapping("/{productId}")
    public AjaxResult getInfo(@PathVariable Long productId)
    {
        return success(productService.selectMallProductById(productId));
    }

    @PreAuthorize("@ss.hasPermi('mall:product:add')")
    @Log(title = "商品新增", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MallProduct product)
    {
        product.setCreateBy(getUsername());
        return toAjax(productService.insertMallProduct(product));
    }

    @PreAuthorize("@ss.hasPermi('mall:product:edit')")
    @Log(title = "商品修改", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MallProduct product)
    {
        product.setUpdateBy(getUsername());
        return toAjax(productService.updateMallProduct(product));
    }

    @PreAuthorize("@ss.hasPermi('mall:product:remove')")
    @Log(title = "商品删除", businessType = BusinessType.DELETE)
    @DeleteMapping("/{productIds}")
    public AjaxResult remove(@PathVariable Long[] productIds)
    {
        return toAjax(productService.deleteMallProductByIds(productIds));
    }
}
