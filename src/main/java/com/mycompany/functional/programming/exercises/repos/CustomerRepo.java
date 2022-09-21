package com.mycompany.functional.programming.exercises.repos;

import java.util.List;

import com.mycompany.functional.programming.exercises.models.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CustomerRepo extends CrudRepository<Customer, Long> {

	List<Customer> findAll();
}
