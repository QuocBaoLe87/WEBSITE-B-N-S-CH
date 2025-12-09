package com.v.model;

// Plain POJO để giữ tương thích compile; KHÔNG phải Entity nữa
public class BookImage {

    private Long id;
    private String url;
    private Book book;

    public BookImage() {
    }

    public BookImage(Book book, String url) {
        this.book = book;
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}
