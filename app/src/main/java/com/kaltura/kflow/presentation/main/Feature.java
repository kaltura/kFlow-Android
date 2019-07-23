package com.kaltura.kflow.presentation.main;

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
    PRODUCT_PRICE("Product price"),
    SETTINGS("Settings");

    private String text;

    Feature(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
