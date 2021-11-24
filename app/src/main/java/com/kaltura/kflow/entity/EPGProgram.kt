package com.kaltura.kflow.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EPGProgram(val name: String, val startDate: Long, val endDate: Long, val epgID: String): Parcelable