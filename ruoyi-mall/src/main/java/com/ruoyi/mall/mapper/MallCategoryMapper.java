package com.ruoyi.mall.mapper;

import java.util.List;
import com.ruoyi.mall.domain.MallCategory;

public interface MallCategoryMapper
{
    MallCategory selectMallCategoryById(Long categoryId);

    List<MallCategory> selectMallCategoryList(MallCategory category);

    int insertMallCategory(MallCategory category);

    int updateMallCategory(MallCategory category);

    int deleteMallCategoryById(Long categoryId);

    int deleteMallCategoryByIds(Long[] categoryIds);
}
