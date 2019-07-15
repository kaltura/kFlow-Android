package com.kaltura.kflow.ui.main;

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
public enum Feature {

    LOGIN("Login"),
    ANONYMOUS_LOGIN("Anonymous login"),
    REGISTRATION("Registration"),
    VOD("VOD gallery"),
    EPG("EPG (past programs)"),
    LIVE("Live TV"),
    FAVORITES("Favorites"),
    SEARCH("Search"),
    MEDIA_PAGE("Media page"),
    SUBSCRIPTION("Subscription"),
    SETTINGS("Settings");

    private String text;

    Feature(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
