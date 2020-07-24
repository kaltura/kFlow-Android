package com.kaltura.kflow.entity

import android.os.Parcelable
import com.kaltura.client.types.Asset
import kotlinx.android.parcel.Parcelize

/**
 * Created by alex_lytvynenko on 25.07.2020.
 */
@Parcelize
data class WatchedAsset(val asset: Asset, val position: Int) : Parcelable