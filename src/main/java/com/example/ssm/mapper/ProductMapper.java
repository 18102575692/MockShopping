package com.example.ssm.mapper;

import com.example.ssm.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ProductMapper extends Mapper<Product> {
    //查询产品
    Product getProduct(Long id);
    //减库存
    int decreaseProduct(@Param("id") Long id,@Param("quantity") Integer quantity);
}
