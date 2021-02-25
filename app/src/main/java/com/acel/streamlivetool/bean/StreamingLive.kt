package com.acel.streamlivetool.bean

import com.acel.streamlivetool.platform.IPlatform

data class StreamingLive(
        val url: String,
        val nowQualityDescription: QualityDescription?,
        val qualityDescriptionList: List<QualityDescription>?
) {
    data class QualityDescription(
            val description: String,
            val num: Int = 0
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is QualityDescription) return false

            if (num != other.num) return false

            return true
        }

        override fun hashCode(): Int {
            return num
        }
    }
}