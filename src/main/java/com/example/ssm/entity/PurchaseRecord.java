package com.example.ssm.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;
import org.apache.ibatis.type.JdbcType;
import tk.mybatis.mapper.annotation.ColumnType;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

@Alias("PurchaseRecord")
@Data
@Table(name = "t_purchase_record")
public class PurchaseRecord implements Serializable {
    private static final long serialVersionUID = -8498672540673970606L;
    Long id;
    Long userId;
    Long productId;
    @ColumnType(jdbcType = JdbcType.DECIMAL)
    double price;
    Integer quantity;
    @ColumnType(jdbcType = JdbcType.DECIMAL)
    double sum;
    Timestamp purchaseDate;
    String note;
}
