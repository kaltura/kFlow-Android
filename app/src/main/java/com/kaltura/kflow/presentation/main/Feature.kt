package com.kaltura.kflow.presentation.main

import androidx.annotation.DrawableRes
import com.kaltura.kflow.R

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
enum class Feature(val text: String, @DrawableRes val imageResId: Int) {
    LOGIN("Login", R.drawable.ic_login),
    ANONYMOUS_LOGIN("Anonymous\nlogin", R.drawable.ic_anonymous),
    REGISTRATION("Registration", R.drawable.ic_registration),
    VOD("VOD gallery", -1),
    EPG("EPG", -1),
    LIVE("Live TV", -1),
    FAVORITES("Favorites", R.drawable.ic_favorite),
    SEARCH("Search", R.drawable.ic_search),
    MEDIA_PAGE("Media page", -1),
    KEEP_ALIVE("Keep Alive", -1),
    SUBSCRIPTION("Subscription", -1),
    PRODUCT_PRICE("Product price", -1),
    CHECK_RECEIPT("Check receipt", R.drawable.ic_check_receipt),
    TRANSACTION_HISTORY("Transaction\nhistory", R.drawable.ic_transaction_history),
    RECORDINGS("Recordings", -1),
    SETTINGS("Settings", R.drawable.ic_settings);
}