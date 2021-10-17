package com.spiet.learnkafka.intg.controller;

import com.spiet.learnkafka.domain.Book;
import com.spiet.learnkafka.domain.LibraryEvent;
import com.spiet.learnkafka.domain.LibraryEventType;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"bookstore-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventsControllerIntegrationTest {

    @Autowired
    private TestRestTemplate request;


    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<Integer, String> consumer;


    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void afterAll() {
        consumer.close();
    }

    @Test
    @DisplayName("integration test to endpoint post")
    @Timeout(5)
    void postLibraryEvent() throws InterruptedException {
        //given
        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("John Doe")
                .bookName("A culpa é do Jon Doe")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .libraryEventType(LibraryEventType.NEW)
                .book(book)
                .build();


        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("content-type", MediaType.APPLICATION_JSON.toString());

        HttpEntity<LibraryEvent> body = new HttpEntity<>(libraryEvent, httpHeaders);

        //when
        var res = request.exchange("/v1/libraryevent", HttpMethod.POST, body, LibraryEvent.class);

        //then
        Assertions.assertEquals(HttpStatus.CREATED, res.getStatusCode());

        var consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "bookstore-events");
        //Thread.sleep(3000);
        String expectedRecord = "{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":123,\"bookName\":\"A culpa é do Jon Doe\",\"bookAuthor\":\"John Doe\"}}";
        String value = consumerRecord.value();
        Assertions.assertEquals(expectedRecord, value);

    }


}
