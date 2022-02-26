package com.spiet.consumer.repository;

import com.spiet.consumer.entity.LibraryEvent;
import org.springframework.data.repository.CrudRepository;

public interface ProductsEventsRepository extends CrudRepository<LibraryEvent, Integer> {
}
