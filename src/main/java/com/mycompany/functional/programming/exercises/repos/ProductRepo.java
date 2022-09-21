package com.mycompany.functional.programming.exercises.repos;

import java.util.List;

import com.mycompany.functional.programming.exercises.models.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepo extends CrudRepository<Product, Long> {

	List<Product> findAll();
}
