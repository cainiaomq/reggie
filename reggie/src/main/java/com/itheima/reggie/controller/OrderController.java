package com.itheima.reggie.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.OrderDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 订单
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;


    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 订单信息分页
     * @param page
     * @param pageSize
     * @param  number
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, Long number,@DateTimeFormat String beginTime,@DateTimeFormat String endTime){
        log.info(String.valueOf(beginTime));
        //构造分页构造器对象
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrderDto> orderDtoPage =new Page<>();
        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(number != null,Orders::getNumber,number);
        if(beginTime !=null && endTime != null){
            queryWrapper.between(Orders::getOrderTime,beginTime,endTime);
        }
        Long currentId = BaseContext.getCurrentId();
        User byId = userService.getById(currentId);
        if(byId != null){
            queryWrapper.eq(Orders::getUserId,currentId);
        }
        //添加排序条件
        queryWrapper.orderByDesc(Orders::getOrderTime);

        orderService.page(pageInfo,queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,orderDtoPage,"records");

        List<Orders> records = pageInfo.getRecords();

        List<OrderDto> list = records.stream().map((item)->{
            AtomicInteger sumnum = new AtomicInteger(0);
            OrderDto orderDto = new OrderDto();
            BeanUtils.copyProperties(item,orderDto);
            Long id = item.getId();
            List<OrderDetail> detail = orderDetailService.getOneList(id);
            if(detail != null){
                orderDto.setOrderDetails(detail);
                for(OrderDetail d:detail){
                    sumnum.addAndGet((new BigDecimal(d.getNumber())).intValue());
                }
                orderDto.setSumNum(new BigDecimal(sumnum.get()));
            }
            return orderDto;
        }).collect(Collectors.toList());
        orderDtoPage.setRecords(list);
        return R.success(orderDtoPage);
    }

    @PutMapping
    public R<String> status(@RequestBody Orders orders){
        log.info(orders.toString());
        Integer status = orders.getStatus();
        Long id = orders.getId();
        if(status == 3){
            LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Orders::getId,id);
            updateWrapper.set(Orders::getStatus,3);
            orderService.update(updateWrapper);
        }else{
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Orders::getId,id);
            orderService.remove(queryWrapper);
            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderDetail::getOrderId,id);
            orderDetailService.remove(wrapper);
        }
        return R.success("修改成功");
    }

    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){
        log.info(orders.toString());
        Long id = orders.getId();
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId,id);
        updateWrapper.set(Orders::getStatus,1);
        updateWrapper.set(Orders::getOrderTime, LocalDateTime.now());
        orderService.update(updateWrapper);
        return R.success("修改成功");
    }
}
