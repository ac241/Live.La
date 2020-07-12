package com.acel.streamlivetool.bean

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
open class Anchor() {
    lateinit var platform: String
    lateinit var nickname: String
    lateinit var showId: String
    lateinit var roomId: String
    var otherParams: String = ""

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    fun anchorKey(): String = platform + roomId

    constructor(
        platform: String,
        nickname: String,
        showId: String,
        roomId: String,
        otherParams: String = ""
    ) : this() {
        this.platform = platform
        this.nickname = nickname
        this.showId = showId
        this.roomId = roomId
        this.otherParams = otherParams
    }

    override fun equals(other: Any?): Boolean {
        return if (other != null) {
            val objAnchor =
                other as Anchor
            platform == objAnchor.platform && roomId == objAnchor.roomId
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = roomId.hashCode()
        result = 31 * result + platform.hashCode()
        result = 31 * result + showId.hashCode()
        return result
    }

    override fun toString(): String {
        return "platform=$platform,nickname=$nickname,roomId=$roomId,showId=$showId,otherParams=$otherParams"
    }
}
