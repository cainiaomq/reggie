package com.itheima.reggie.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单明细
 */
@RestController
@RequestMapping("/orderdetail")
@Slf4j
public class OrderdetailController {

    @Autowired
    private OrderdetailController orderdetailController;
}
