package com.example.ssm.mapper;

import com.example.ssm.entity.PurchaseRecord;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface PurchaseRecordMapper extends Mapper<PurchaseRecord> {
}
