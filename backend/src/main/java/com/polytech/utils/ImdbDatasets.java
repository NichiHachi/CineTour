package com.polytech.utils;

public enum ImdbDatasets {
    MOVIE_BASICS("https://datasets.imdbws.com/title.basics.tsv.gz", "title.basics.tsv.gz"),
    PRINCIPALS("https://datasets.imdbws.com/title.principals.tsv.gz", "title.principals.tsv.gz"),
    NAMES("https://datasets.imdbws.com/name.basics.tsv.gz", "name.basics.tsv.gz"),
    CREW("https://datasets.imdbws.com/title.crew.tsv.gz", "title.crew.tsv.gz"),
    RATINGS("https://datasets.imdbws.com/title.ratings.tsv.gz", "title.ratings.tsv.gz");

    private final String url;
    private final String fileName;

    ImdbDatasets(String url, String fileName) {
        this.url = url;
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }
}

