package com.example.chad.booksearch;


import java.io.Serializable;

/**
 * Created by Chad on 12/3/2016.
 */

public class Book implements Serializable {

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
