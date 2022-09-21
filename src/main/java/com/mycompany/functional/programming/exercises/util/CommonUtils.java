package com.mycompany.functional.programming.exercises.util;

import com.mycompany.functional.programming.exercises.repos.ProductRepo;

import java.util.Comparator;

public class CommonUtils {

    public static Double getUpperPriceLimit(ProductRepo productRepos) {
        return productRepos.findAll().stream().max(Comparator.
                comparing(com.mycompany.functional.programming.exercises.models.Product::getPrice)).get().getPrice();
    }
}
