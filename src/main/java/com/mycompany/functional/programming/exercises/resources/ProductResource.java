package com.mycompany.functional.programming.exercises.resources;

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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
public class ProductResource {

    Logger logger = LoggerFactory.getLogger(ProductResource.class);
    @Autowired
    private ProductRepo productRepos;

    @Autowired
    private OrderRepo orderRepo;

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

    public Optional<Product> getCheapestBook(List<Product> productsList) {
        return productsList.stream().filter(p -> "Books".equalsIgnoreCase(p.getCategory())).min(Comparator.
                comparing(Product::getPrice));
    }

}
