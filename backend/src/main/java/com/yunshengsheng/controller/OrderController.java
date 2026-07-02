package com.yunshengsheng.controller;

import com.yunshengsheng.common.ApiResponse;
import com.yunshengsheng.domain.Models.Order;
import com.yunshengsheng.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/list")
    public ApiResponse<List<Order>> list() {
        return ApiResponse.success(orderService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<Order> detail(@PathVariable Long id) {
        return ApiResponse.success(orderService.detail(id));
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<Order> confirm(@PathVariable Long id) {
        return ApiResponse.success(orderService.confirm(id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Order> cancel(@PathVariable Long id) {
        return ApiResponse.success(orderService.cancel(id));
    }
}
