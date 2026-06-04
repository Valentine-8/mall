package com.ruoyi.mall.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.domain.MallCategory;
import com.ruoyi.mall.mapper.MallCategoryMapper;
import com.ruoyi.mall.service.IMallCategoryService;

@Service
public class MallCategoryServiceImpl implements IMallCategoryService
{
    @Autowired
    private MallCategoryMapper categoryMapper;

    @Override
    public MallCategory selectMallCategoryById(Long categoryId)
    {
        return categoryMapper.selectMallCategoryById(categoryId);
    }

    @Override
    public List<MallCategory> selectMallCategoryList(MallCategory category)
    {
        return categoryMapper.selectMallCategoryList(category);
    }

    @Override
    public int insertMallCategory(MallCategory category)
    {
        if (category.getParentId() == null)
        {
            category.setParentId(0L);
        }
        if (category.getOrderNum() == null)
        {
            category.setOrderNum(0);
        }
        if (StringUtils.isEmpty(category.getStatus()))
        {
            category.setStatus("0");
        }
        return categoryMapper.insertMallCategory(category);
    }

    @Override
    public int updateMallCategory(MallCategory category)
    {
        return categoryMapper.updateMallCategory(category);
    }

    @Override
    public int deleteMallCategoryByIds(Long[] categoryIds)
    {
        return categoryMapper.deleteMallCategoryByIds(categoryIds);
    }
}
