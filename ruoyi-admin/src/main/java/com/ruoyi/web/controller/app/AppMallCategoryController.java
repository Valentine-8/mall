package com.ruoyi.web.controller.app;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.domain.MallCategory;
import com.ruoyi.mall.service.IMallCategoryService;

/**
 * C-end mall category (public browse)
 */
@RestController
@RequestMapping("/app/mall/category")
public class AppMallCategoryController extends BaseController
{
    @Autowired
    private IMallCategoryService categoryService;

    @GetMapping("/list")
    public AjaxResult list(MallCategory category)
    {
        category.setStatus("0");
        List<MallCategory> list = categoryService.selectMallCategoryList(category);
        return success(list);
    }
}
