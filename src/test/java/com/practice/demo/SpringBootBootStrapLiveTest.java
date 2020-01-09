package com.practice.demo;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.practice.demo.persistence.model.Book;


import io.restassured.RestAssured;
import io.restassured.response.Response;

public class SpringBootBootStrapLiveTest {

    private static final String API_ROOT = "http://localhost:8081/api/books";

    @Test
    public void whenGetAllBooksThenOk() {
        Response response = RestAssured.get(API_ROOT);

        assertThat(HttpStatus.OK.value(), is(response.getStatusCode()));
    }

    @Test
    public void whenGetBooksByTitleThenOk() {
        Book book = createRandomBook();
        createBookAsUri(book);
        Response response = RestAssured.get(API_ROOT + "/title/" + book.getTitle());

        assertThat(HttpStatus.OK.value(), is(response.getStatusCode()));
//        assertTrue(response.as(List.class).size() > 0);
    }

    @Test
    public void whenGetCreatedBookByIdThenOk() {
        Book book = createRandomBook();
        String location = createBookAsUri(book);
        Response response = RestAssured.get(location);

        assertThat(HttpStatus.OK.value(), is(response.getStatusCode()));
        assertThat(book.getTitle(), is(response.jsonPath().get("title")));
    }

    @Test
    public void whenGetNotExistBookByIdThenNotFound() {
        Response response = RestAssured.get(API_ROOT + "/" + randomNumeric(4));

        assertThat(HttpStatus.NOT_FOUND.value(), is(response.getStatusCode()));
    }

    @Test
    public void whenCreateNewBookThenCreated() {
        Book book = createRandomBook();
        Response response = RestAssured.given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(book)
            .post(API_ROOT);

        assertThat(HttpStatus.CREATED.value(), is(response.getStatusCode()));
    }

     @Test
     public void whenInvalidBookThenError() {
         Book book = createRandomBook();
         book.setAuthor(null);
         Response response = RestAssured.given()
             .contentType(MediaType.APPLICATION_JSON_VALUE)
             .body(book)
             .post(API_ROOT);

         assertThat(HttpStatus.BAD_REQUEST.value(), is(response.getStatusCode()));
     }

     @Test
     public void whenUpdateCreatedBookThenUpdate() {
         Book book = createRandomBook();
         String location = createBookAsUri(book);
         book.setId(Long.parseLong(location.split("/api/books/")[1]));
         book.setAuthor("new Author");
         Response response = RestAssured.given()
             .contentType(MediaType.APPLICATION_JSON_VALUE)
             .body(book)
             .post(location);

         assertThat(HttpStatus.OK.value(), is(response.getStatusCode()));
         assertThat("new Author", is(response.jsonPath().get("author")));
     }

    @Test
    public void whenDeleteCreatedBook_thenOk() {
        Book book = createRandomBook();
        String location = createBookAsUri(book);
        Response response = RestAssured.delete(location);

        assertThat(HttpStatus.OK.value(), is(response.getStatusCode()));

        response = RestAssured.get(location);
        assertThat(HttpStatus.NOT_FOUND.value(), is(response.getStatusCode()));
    }

    private Book createRandomBook() {
        Book book = new Book();
        book.setTitle(randomAlphabetic(10));
        book.setAuthor(randomAlphabetic(15));
        return book;
    }

    private String createBookAsUri(Book book) {
        Response response = RestAssured.given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(book)
            .post(API_ROOT);
        return API_ROOT + "/" + response.jsonPath().get("id");
    }
}
