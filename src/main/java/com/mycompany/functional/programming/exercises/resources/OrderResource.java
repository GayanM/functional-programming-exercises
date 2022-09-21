package com.mycompany.functional.programming.exercises.resources;

import com.mycompany.functional.programming.exercises.dto.Order;
import com.mycompany.functional.programming.exercises.repos.OrderRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderResource {
    Logger logger = LoggerFactory.getLogger(OrderResource.class);
    @Autowired
    private OrderRepo orderRepo;

    @RequestMapping("")
    public List<Order> getOrdersByProductCategory (@RequestParam("category") final Optional<String> category) {
        return orderRepo.findAll().stream().
                filter(o -> o.getProducts().stream().anyMatch(p -> {
                    if (category.isEmpty()) {
                        return true;
                    }
                    return category.get().equalsIgnoreCase(p.getCategory());
                })).
                map(o -> new Order(o.getId(), o.getOrderDate(), o.getDeliveryDate(), o.getStatus())).
                collect(Collectors.toList());
    }
}
