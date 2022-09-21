package com.mycompany.functional.programming.exercises.resources;

import com.mycompany.functional.programming.exercises.dto.Product;
import com.mycompany.functional.programming.exercises.repos.ProductRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
public class ProductResource {

    Logger logger = LoggerFactory.getLogger(ProductResource.class);
    @Autowired
    private ProductRepo productRepos;

    @RequestMapping("")
    public List<Product> filterProducts(@Param("category") final Optional<String> category,
                                        @Param("lower-price-limit") final Optional<Double> lowerLimitPrice,
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
}
