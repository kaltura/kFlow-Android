package com.kaltura.kflow.entity
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChannelCS(val name: String, val id: String, val descripion: String?, val lcn: Int): Parcelable