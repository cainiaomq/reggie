package com.itheima.reggie.dto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderDto extends Orders {
    private List<OrderDetail> orderDetails = new ArrayList<>();

    private BigDecimal sumNum;
}
