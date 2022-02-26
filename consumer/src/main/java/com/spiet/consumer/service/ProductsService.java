package com.spiet.consumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spiet.consumer.entity.LibraryEvent;
import com.spiet.consumer.repository.ProductsEventsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@Slf4j
public class ProductsService {

    @Autowired
    private ObjectMapper objectMapper;

    private final ProductsEventsRepository repository;

    public ProductsService(ProductsEventsRepository repository) {
        this.repository = repository;
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        repository.save(libraryEvent);
        log.info("Successfully Persisted the libary Event {} ", libraryEvent);
    }

    public void processLibraryEvent(ConsumerRecord<Integer, String> record) throws JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(record.value(), LibraryEvent.class);

        log.info("Library {}", libraryEvent);

        switch (libraryEvent.getLibraryEventType()) {
            case NEW: save(libraryEvent);
            break;

            case UPDATE:
                // update operation
                break;
        }
    }
}
