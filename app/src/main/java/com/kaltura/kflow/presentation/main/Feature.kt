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
    VOD("VOD gallery", R.drawable.ic_vod),
    EPG("EPG", -1),
    LIVE("Live TV", R.drawable.ic_live_tv),
    FAVORITES("Favorites", R.drawable.ic_favorite),
    SEARCH("Search", R.drawable.ic_search),
    MEDIA_PAGE("Media page", R.drawable.ic_media_page),
    KEEP_ALIVE("Keep Alive", R.drawable.ic_media_page),
    SUBSCRIPTION("Subscription", R.drawable.ic_subscription),
    PRODUCT_PRICE("Product price", R.drawable.ic_product_price),
    CHECK_RECEIPT("Check receipt", R.drawable.ic_check_receipt),
    TRANSACTION_HISTORY("Transaction\nhistory", R.drawable.ic_transaction_history),
    RECORDINGS("Recordings", R.drawable.ic_recordings),
    SETTINGS("Settings", R.drawable.ic_settings);
}