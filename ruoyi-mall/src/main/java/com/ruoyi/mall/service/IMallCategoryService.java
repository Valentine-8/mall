package com.ruoyi.mall.service;

import java.util.List;
import com.ruoyi.mall.domain.MallCategory;

public interface IMallCategoryService
{
    MallCategory selectMallCategoryById(Long categoryId);

    List<MallCategory> selectMallCategoryList(MallCategory category);

    int insertMallCategory(MallCategory category);

    int updateMallCategory(MallCategory category);

    int deleteMallCategoryByIds(Long[] categoryIds);
}
