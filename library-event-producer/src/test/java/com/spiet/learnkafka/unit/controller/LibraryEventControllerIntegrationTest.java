package com.spiet.learnkafka.unit.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spiet.learnkafka.controller.LibraryEventsController;
import com.spiet.learnkafka.domain.Book;
import com.spiet.learnkafka.domain.LibraryEvent;
import com.spiet.learnkafka.domain.LibraryEventType;
import com.spiet.learnkafka.producer.LibraryEventProducer;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventsController.class)
@AutoConfigureMockMvc
public class LibraryEventControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LibraryEventProducer libraryEventProducer;

    @Test
    void postLibraryEvent() throws Exception {
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

        BDDMockito.given(libraryEventProducer.sendLibraryEvent_approach2(any(LibraryEvent.class))).willReturn(null);
        //when
            var request = objectMapper.writeValueAsString(libraryEvent);
            mockMvc.perform(post("/v1/libraryevent")
                    .content(request)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isCreated());
        //then

    }


    @Test
    void postLibraryEvent_4xx() throws Exception {
        //given
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .libraryEventType(LibraryEventType.NEW)
                .book(null)
                .build();

        BDDMockito.given(libraryEventProducer.sendLibraryEvent_approach2(any(LibraryEvent.class))).willReturn(null);
        //when
        var request = objectMapper.writeValueAsString(libraryEvent);
        mockMvc.perform(post("/v1/libraryevent")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is4xxClientError());
        //then

    }
}
