package com.fastbite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    // Vistas de cliente
    @GetMapping("/customer/menu")
    public String customerMenu() {
        return "customer/menu";
    }

    @GetMapping("/customer/orders")
    public String customerOrders() {
        return "customer/orders";
    }

    @GetMapping("/customer/order/{id}")
    public String customerOrderDetail() {
        return "customer/order-detail";
    }

    // Vistas de cocina
    @GetMapping("/kitchen/dashboard")
    public String kitchenDashboard() {
        return "kitchen/dashboard";
    }

    // Vistas de repartidor (para después)
    @GetMapping("/delivery/dashboard")
    public String deliveryDashboard() {
        return "delivery/dashboard";
    }
}
