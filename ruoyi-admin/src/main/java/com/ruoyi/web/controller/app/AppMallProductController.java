package com.ruoyi.web.controller.app;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mall.domain.MallProduct;
import com.ruoyi.mall.service.IMallProductService;

/**
 * C-end mall product (public browse)
 */
@RestController
@RequestMapping("/app/mall/product")
public class AppMallProductController extends BaseController
{
    @Autowired
    private IMallProductService productService;

    @GetMapping("/list")
    public TableDataInfo list(MallProduct product)
    {
        product.setStatus("0");
        startPage();
        List<MallProduct> list = productService.selectMallProductList(product);
        return getDataTable(list);
    }

    @GetMapping("/{productId}")
    public AjaxResult getInfo(@PathVariable Long productId)
    {
        MallProduct product = productService.selectMallProductById(productId);
        if (product == null || !"0".equals(product.getStatus()))
        {
            return error("商品不存在或已下架");
        }
        return success(product);
    }
}
