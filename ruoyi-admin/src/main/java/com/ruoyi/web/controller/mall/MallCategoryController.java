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
import com.ruoyi.mall.domain.MallCategory;
import com.ruoyi.mall.service.IMallCategoryService;

@RestController
@RequestMapping("/mall/category")
public class MallCategoryController extends BaseController
{
    @Autowired
    private IMallCategoryService categoryService;

    @PreAuthorize("@ss.hasPermi('mall:category:list')")
    @GetMapping("/list")
    public TableDataInfo list(MallCategory category)
    {
        startPage();
        List<MallCategory> list = categoryService.selectMallCategoryList(category);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:category:list')")
    @GetMapping("/listAll")
    public AjaxResult listAll(MallCategory category)
    {
        return success(categoryService.selectMallCategoryList(category));
    }

    @PreAuthorize("@ss.hasPermi('mall:category:query')")
    @GetMapping("/{categoryId}")
    public AjaxResult getInfo(@PathVariable Long categoryId)
    {
        return success(categoryService.selectMallCategoryById(categoryId));
    }

    @PreAuthorize("@ss.hasPermi('mall:category:add')")
    @Log(title = "商品分类新增", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MallCategory category)
    {
        category.setCreateBy(getUsername());
        return toAjax(categoryService.insertMallCategory(category));
    }

    @PreAuthorize("@ss.hasPermi('mall:category:edit')")
    @Log(title = "商品分类修改", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MallCategory category)
    {
        category.setUpdateBy(getUsername());
        return toAjax(categoryService.updateMallCategory(category));
    }

    @PreAuthorize("@ss.hasPermi('mall:category:remove')")
    @Log(title = "商品分类删除", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(categoryService.deleteMallCategoryByIds(categoryIds));
    }
}
