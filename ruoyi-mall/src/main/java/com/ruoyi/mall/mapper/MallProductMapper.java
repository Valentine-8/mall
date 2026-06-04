package com.ruoyi.mall.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.domain.MallProduct;

public interface MallProductMapper
{
    MallProduct selectMallProductById(Long productId);

    List<MallProduct> selectMallProductList(MallProduct product);

    int insertMallProduct(MallProduct product);

    int updateMallProduct(MallProduct product);

    int deleteMallProductById(Long productId);

    int deleteMallProductByIds(Long[] productIds);

    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    int increaseSaleCount(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    int restoreStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
