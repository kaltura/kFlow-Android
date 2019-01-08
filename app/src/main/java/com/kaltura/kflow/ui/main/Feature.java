package com.kaltura.kflow.ui.main;

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
public enum Feature {

    LOGIN("Login"),
    ANONYMOUS_LOGIN("Anonymous login"),
    REGISTRATION("Registration"),
    VOD("VOD gallery"),
    FAVORITES("Favorites"),
    SEARCH("Search");

    private String text;

    Feature(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
