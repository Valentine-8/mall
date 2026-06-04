package com.ruoyi.mall.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.domain.MallProduct;
import com.ruoyi.mall.mapper.MallProductMapper;
import com.ruoyi.mall.service.IMallProductService;

@Service
public class MallProductServiceImpl implements IMallProductService
{
    @Autowired
    private MallProductMapper productMapper;

    @Override
    public MallProduct selectMallProductById(Long productId)
    {
        return productMapper.selectMallProductById(productId);
    }

    @Override
    public List<MallProduct> selectMallProductList(MallProduct product)
    {
        return productMapper.selectMallProductList(product);
    }

    @Override
    public int insertMallProduct(MallProduct product)
    {
        if (product.getStock() == null)
        {
            product.setStock(0);
        }
        if (product.getSaleCount() == null)
        {
            product.setSaleCount(0);
        }
        if (StringUtils.isEmpty(product.getStatus()))
        {
            product.setStatus("0");
        }
        return productMapper.insertMallProduct(product);
    }

    @Override
    public int updateMallProduct(MallProduct product)
    {
        return productMapper.updateMallProduct(product);
    }

    @Override
    public int deleteMallProductByIds(Long[] productIds)
    {
        return productMapper.deleteMallProductByIds(productIds);
    }
}
