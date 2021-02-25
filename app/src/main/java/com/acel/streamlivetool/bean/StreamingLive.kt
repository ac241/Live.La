package com.acel.streamlivetool.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class StreamingLive(
    val url: String,
    val currentQuality: Quality?,
    val qualityList: List<Quality>?
) {
    @Parcelize
    data class Quality(
            val description: String,
            val num: Int = 0
    ) : Parcelable {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Quality) return false

            if (num != other.num) return false

            return true
        }

        override fun hashCode(): Int {
            return num
        }
    }
}