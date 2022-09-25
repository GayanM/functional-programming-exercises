package com.mycompany.functional.programming.exercises.resources;

import com.mycompany.functional.programming.exercises.dto.Customer;
import com.mycompany.functional.programming.exercises.dto.Order;
import com.mycompany.functional.programming.exercises.dto.Product;
import com.mycompany.functional.programming.exercises.repos.OrderRepo;
import com.mycompany.functional.programming.exercises.repos.ProductRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
public class ProductResource {

    Logger logger = LoggerFactory.getLogger(ProductResource.class);
    @Autowired
    private ProductRepo productRepos;

    @Autowired
    private OrderRepo orderRepo;

    //Ex-1: Filter products by category and price range
    @RequestMapping("")
    public List<Product> filterProducts(@RequestParam("category") final Optional<String> category,
                                        @RequestParam("lower-price-limit") final Optional<Double> lowerLimitPrice,
                                        @RequestParam("upper-price-limit") final Optional<Double> upperLimitPrice ) {


        return productRepos.findAll().stream().
                filter(p -> {
                    if (category.isEmpty()) {
                        return true;
                    }
                    return category.get().equals(p.getCategory());
                }).
                filter(p -> {
                    if (lowerLimitPrice.isEmpty()) {
                        return true;
                    }
                    return lowerLimitPrice.get() < p.getPrice();
                }).
                filter(p -> {
                    if (upperLimitPrice.isEmpty()) {
                        return true;
                    }
                    return upperLimitPrice.get() > p.getPrice();
                }).
                map(p -> {
                        return new Product(p.getId(), p.getName(), p.getPrice(), p.getCategory());
                    }
                ).collect(Collectors.toList());
    }

    //Ex-3:Obtain a list of product with given category and then apply 10% discount
    @RequestMapping("/discount")
    public List<Product> applyDiscountOnCategory(@RequestParam("category") final Optional<String> category,
                                                 @RequestParam("discount") final Optional<Double> discount) {
        return productRepos.findAll().stream().
               filter(p -> {
                   if (category.isEmpty()) {
                       return true;
                   }
                   return category.get().equalsIgnoreCase(p.getCategory());
               }).map(p -> {
                   if (discount.isEmpty()) {
                       return p;
                   }
                   p.setPrice(p.getPrice() * ((100 - discount.get())/100));
                   return p;
               }).map(
                   p -> {
                       return new Product(p.getId(), p.getName(), p.getPrice(), p.getCategory());
                   }
                ).collect(Collectors.toList());
    }

    //Ex-4: Obtain a list of products based on customer tier and date range
    @RequestMapping("/customer-tier")
    public List<Product> getProductsByCustomerTier (@RequestParam("tier") final Optional<Integer> tier,
                                                    @RequestParam("start-date") final Optional<String> startDate,
                                                    @RequestParam("end-date") final Optional<String> endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return orderRepo.findAll().stream().
               filter(o -> {
                   if (tier.isEmpty()) {
                       return true;
                   }
                   return tier.get().equals(o.getCustomer().getTier());
               }).
               filter(o -> {
                   if (startDate.isEmpty()) {
                       return true;
                   }
                   return LocalDate.parse(startDate.get(), formatter).isBefore(o.getOrderDate()) ||
                           LocalDate.parse(startDate.get(), formatter).equals(o.getOrderDate()) ;
               }).
               filter(o -> {
                   if (endDate.isEmpty()) {
                       return true;
                   }
                   return LocalDate.parse(endDate.get(), formatter).isAfter(o.getOrderDate()) ||
                           LocalDate.parse(endDate.get(), formatter).equals(o.getOrderDate());
               }).
               flatMap(o -> o.getProducts().stream()).
               distinct().
               map(p -> {
                    return new Product(p.getId(), p.getName(), p.getPrice(), p.getCategory());
               }).
               collect(Collectors.toList());
    }


    //Ex-5: Get the cheapest products by category
    @RequestMapping("/cheapest")
    public Optional<Product> getCheapestProductByCategory(@RequestParam("category") final Optional<String> category) {
        return productRepos.findAll().stream().filter(p -> {
            if (category.isEmpty()) {
                return true;
            }
            return category.get().equalsIgnoreCase(p.getCategory());
        }).min(Comparator.comparing(com.mycompany.functional.programming.exercises.models.Product::getPrice)).map(p ->
                new Product(p.getId(), p.getName(), p.getPrice(), p.getCategory()));
    }

    //Ex-7: Get a list of orders which were ordered on 15-Mar-2021, log the order records to the console and then return
    // its product list
    @RequestMapping("/log-orders")
    public List<Product> logOrdersOnGivenDateAndReturnProducts(@RequestParam("date") final Optional<String> date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return orderRepo.findAll().stream()
                .filter(o -> o.getOrderDate().isEqual(LocalDate.parse(date.get(), formatter)))
                .peek(o -> System.out.println(o.toString()))
                .flatMap(o -> o.getProducts().stream())
                .distinct().map(p -> new Product(p.getId(), p.getName(), p.getPrice(), p.getCategory()))
                .collect(Collectors.toList());
    }

    //Ex-10: Obtain a collection of statistic figures (i.e. sum, average, max, min, count) for all products of
    // category “Books”

    @RequestMapping("/statistics")
    public DoubleSummaryStatistics getStatistics(@RequestParam("category") final Optional<String> category) {
        return productRepos.findAll().stream().filter(p -> category.get().equalsIgnoreCase(p.getCategory())).
                mapToDouble(p -> p.getPrice()).summaryStatistics();
    }

    //Ex-14: Obtain a data map with list of product name by category
    @RequestMapping("/product-category")
    Map<String, List<String>> getProductsByCategory(List<Product> productList) {
        return productRepos.findAll().stream().collect(Collectors.groupingBy(com.mycompany.functional.programming.
                        exercises.models.Product::getCategory, Collectors.mapping(product -> product.getName(),
                Collectors.toList())));
    }

    //Ex-15: Get the most expensive product by category
    @RequestMapping("/most-expensive")
    Map<String, Optional<Product>> getMostExpensiveProductByCategory () {
        Map<String, Optional<com.mycompany.functional.programming.exercises.models.Product>> mostExpensiveByCategory = productRepos.findAll().stream().collect(Collectors.groupingBy(
                    com.mycompany.functional.programming.exercises.models.Product::getCategory,
                    Collectors.maxBy(Comparator.comparing(com.mycompany.functional.programming.exercises.models.
                            Product::getPrice)
                 )
         ));
        Map<String, Optional<Product>> ordersByCustomerTransformed = new HashMap();
        mostExpensiveByCategory.entrySet().stream().forEach(e -> {
            ordersByCustomerTransformed.put(e.getKey(), Optional.of(new Product(e.getValue().get().getId(),
                    e.getValue().get().getName(), e.getValue().get().getPrice(), e.getValue().get().getCategory())));
        });
        return ordersByCustomerTransformed;
    }

    //Ex-15: Get the most expensive product by category
    Map<String, Optional<Product>> getMostExpensiveProductByCategory (List<Product> productList) {
        return productList.stream().collect(Collectors.groupingBy(Product::getCategory, Collectors.maxBy(Comparator.comparing(Product::getPrice))));
    }

}
