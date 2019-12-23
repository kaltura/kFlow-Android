package com.kaltura.kflow.presentation.extension

import com.kaltura.client.types.Asset
import java.util.*

/**
 * Created by alex_lytvynenko on 2019-12-23.
 */
fun Asset.isProgramInPast() = endDate < Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis / 1000

fun Asset.isProgramInFuture() = startDate > Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis / 1000

fun Asset.isProgramInLive(): Boolean {
    val currentTimeMs = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis / 1000
    return (currentTimeMs in (startDate + 1) until endDate)
}