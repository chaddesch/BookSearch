package com.example.chad.booksearch;

/**
 * Created by Chad on 12/3/2016.
 */

public class Book {

    private String mTitle;

    private String mAuthor;

    public Book(String title, String author) {
        mTitle = title;
        mAuthor = author;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

}
