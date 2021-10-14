package com.spiet.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spiet.learnkafka.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class LibraryEventProducer {

    @Autowired
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String topic = "bookstore-events";

    public ListenableFuture<SendResult<Integer, String>> sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key = libraryEvent.getLibraryEventId();
        var value = objectMapper.writeValueAsString(libraryEvent);

        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);

        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleError(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
        });

        return listenableFuture;
    }

    public ListenableFuture<SendResult<Integer, String>> sendLibraryEvent_approach2(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key = libraryEvent.getLibraryEventId();
        var value = objectMapper.writeValueAsString(libraryEvent);

        ProducerRecord<Integer, String> producerRecord = buildProducerFactory(key, value, topic);

        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);

        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleError(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
        });

        return listenableFuture;
    }

    private ProducerRecord<Integer, String> buildProducerFactory(Integer key, String value, String topic) {
        return new ProducerRecord<>(topic, null, key, value, null);
    }


    private void handleError(Integer key, String value, Throwable ex) {
        try {
            throw ex;
        } catch (Throwable e) {
            log.info("ERROR ON SENT MESSAGE for the key: {}, and value: {} and partition is {}", key, value, e.getMessage());
        }

    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("MESSAGE SENT SUCESSFULY for the key: {}, and value: {} and partition is {}", key, value, result.getRecordMetadata());
    }

    public SendResult<Integer, String> sendLibraryEventSynchronus(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key = libraryEvent.getLibraryEventId();
        var value = objectMapper.writeValueAsString(libraryEvent);
        SendResult<Integer, String> sendResult = null;
        try {
            sendResult =  kafkaTemplate.sendDefault(key, value).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sendResult;
    }
}
