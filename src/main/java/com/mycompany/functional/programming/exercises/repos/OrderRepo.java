package com.mycompany.functional.programming.exercises.repos;

import java.util.List;

import com.mycompany.functional.programming.exercises.models.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepo extends CrudRepository<Order, Long> {

	List<Order> findAll();
}
