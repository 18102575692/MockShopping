package com.example.ssm.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;
import org.apache.ibatis.type.JdbcType;
import tk.mybatis.mapper.annotation.ColumnType;

import javax.persistence.Table;
import java.io.Serializable;

@Alias("product")
@Data
@Table(name = "t_product")
public class Product implements Serializable {
    private static final long serialVersionUID = 6147072066529124326L;
    Long id;
    String productName;
    Integer stock;
    @ColumnType(jdbcType = JdbcType.DECIMAL)
    double price;
    Integer version;
    String note;
}
