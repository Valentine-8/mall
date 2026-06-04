package com.ruoyi.mall.service;

import java.util.List;
import com.ruoyi.mall.domain.MallProduct;

public interface IMallProductService
{
    MallProduct selectMallProductById(Long productId);

    List<MallProduct> selectMallProductList(MallProduct product);

    int insertMallProduct(MallProduct product);

    int updateMallProduct(MallProduct product);

    int deleteMallProductByIds(Long[] productIds);
}
