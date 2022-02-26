package com.spiet.consumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spiet.consumer.service.ProductsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProducsConsumer {

    @Autowired
    private ProductsService service;

    @KafkaListener(topics = {"bookstore-events"})
    public void readMessage(ConsumerRecord<Integer, String> record) throws JsonProcessingException {
        log.info("Consumer Record: {}" , record);
        service.processLibraryEvent(record);

    }
}
