package com.example.ssm.service;

import com.example.ssm.entity.PurchaseRecord;

import java.util.List;

public interface PurchaseService {
    /**
     * 处理购买逻辑
     * @param userId 用户信息
     * @param productId 产品信息
     * @param quantity 数量
     * @return 购买结果
     */
     boolean purchase(Long userId,Long productId,int quantity);

    /**
     * 处理购买逻辑
     * @param userId 用户信息
     * @param productId 产品信息
     * @param quantity 数量
     * @return 购买结果
     */
     boolean purchaseLua(Long userId,Long productId,int quantity);

    boolean purchase(List<PurchaseRecord> recordList);
}
