package com.v.model;

public class CartItem {

    private final Book book;
    private int quantity;

    public CartItem(Book book, int quantity) {
        this.book = book;
        this.quantity = quantity;
    }

    public Book getBook() {
        return book;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void increment() {
        this.quantity++;
    }
}
