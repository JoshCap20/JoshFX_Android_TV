package com.example.myapplication;

public class Movie {
    private int id;
    private String title;
    private String link;
    private String stream;

    public Movie(int id, String title, String link, String stream) {
        this.id = id;
        this.title = title;
        this.link = link;
        this.stream = stream;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getStream() {
        return stream;
    }
}
