package com.mycompany.functional.programming.exercises.resources;

import com.mycompany.functional.programming.exercises.dto.Customer;
import com.mycompany.functional.programming.exercises.dto.Order;
import com.mycompany.functional.programming.exercises.repos.OrderRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderResource {
    Logger logger = LoggerFactory.getLogger(OrderResource.class);
    @Autowired
    private OrderRepo orderRepo;

    //Ex-2: Get orders contains given product category
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

    //Ex-6: Get the 3 most recent placed order
    @RequestMapping("/recent")
    public List<Order> getRecentOrders (@RequestParam("limit") final Optional<Long> limit) {
        return orderRepo.findAll().stream().
                sorted(Comparator.
                        comparing(com.mycompany.functional.programming.exercises.models.Order::getOrderDate).
                        reversed()).limit(limit.get()).map(o -> new Order(o.getId(), o.getOrderDate(),
                        o.getDeliveryDate(), o.getStatus())).collect(Collectors.toList());
    }

    //Ex-8: Calculate total lump sum of all orders placed in Feb 2021:Answ=11995.36
    @RequestMapping("/lump-sum")
    public Double lumpSumOfOrders (@RequestParam("month") final int month) {
        return orderRepo.findAll()
                .stream()
                .filter(o -> o.getOrderDate().compareTo(LocalDate.of(2021, month, 1)) >= 0)
                .filter(o -> o.getOrderDate().compareTo(LocalDate.of(2021, month + 1, 1)) < 0)
                .flatMap(o -> o.getProducts().stream())
                .mapToDouble(p -> p.getPrice())
                .sum();

    }

    //Ex-9: Calculate order average payment placed on 14-Mar-2021:Answ=287.604
    @RequestMapping("/avg-payment")
    public Double getAvgPayment(@RequestParam("date") final Optional<String> date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return orderRepo.findAll().stream().filter(o -> o.getOrderDate().isEqual(LocalDate.
                        parse(date.get(), formatter))).flatMap(o -> o.getProducts().stream()).mapToDouble(p ->
                p.getPrice()).average().getAsDouble();
    }

    //Ex-11: Obtain a data map with order id and order’s product count
    @RequestMapping("/products-count")
    public Map<Long, Integer> getOrdersWithProductCount() {
        return orderRepo.findAll().stream().
                collect(Collectors.toMap(o -> o.getId(), o -> o.getProducts().size()));
    }

    //Ex-12: Produce a data map with order records grouped by customer
    @RequestMapping("/group-by-customer")
    public Map<Customer, List<Order>> getOrdersByCustomer () {
        Map<com.mycompany.functional.programming.exercises.models.Customer,
                List<com.mycompany.functional.programming.exercises.models.Order>> ordersByCustomer = orderRepo.
                findAll().stream().collect(Collectors.groupingBy(com.mycompany.functional.programming.exercises.models.
                        Order::getCustomer));
        Map<Customer, List<Order>> ordersByCustomerTransformed = new HashMap();
        ordersByCustomer.forEach((k,v) -> {
            Customer customer = new Customer(k.getId(), k.getName(), k.getTier());
            List<Order> orders = v.stream().map(o -> new Order(o.getId(), o.getOrderDate(), o.getDeliveryDate(), o.getStatus())).
                    collect(Collectors.toList());
            ordersByCustomerTransformed.put(customer, orders);
        });
        return ordersByCustomerTransformed;
    }

    //Ex-13: Produce a data map with order record and product total sum
    @RequestMapping("/product-total-sum")
    public Map<Order, Double> getProductTotalSumByOrder() {
         return orderRepo.findAll().stream().collect(Collectors.toMap(o -> new Order(o.getId(), o.getOrderDate(),
                 o.getDeliveryDate(), o.getStatus()), o -> o.getProducts().stream().mapToDouble(p ->
                 p.getPrice()).sum()));
    }

}
