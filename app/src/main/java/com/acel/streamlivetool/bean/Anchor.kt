package com.acel.streamlivetool.bean

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class Anchor() {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    lateinit var platform: String
    lateinit var nickname: String
    lateinit var showId: String
    lateinit var roomId: String
    var otherParams: String = ""

    @Ignore
    var status: Boolean = false

    @Ignore
    var title: String? = null

    @Ignore
    var avatar: String? = ""

    @Ignore
    var keyFrame: String? = null

    @Ignore
    var secondaryStatus: String? = null

    /**
     * 直播类型
     */
    @Ignore
    var typeName: String? = null

    /**
     * 热度
     */
    @Ignore
    var online: String? = null

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

    constructor(
        platform: String,
        nickname: String,
        showId: String,
        roomId: String,
        status: Boolean,
        title: String? = null,
        avatar: String? = null,
        keyFrame: String? = null,
        otherParams: String = "",
        secondaryStatus: String? = null,
        typeName: String? = null,
        online: String? = null
    ) : this() {
        this.platform = platform
        this.nickname = nickname
        this.showId = showId
        this.roomId = roomId
        this.otherParams = otherParams
        this.status = status
        this.title = title
        this.avatar = avatar
        this.keyFrame = keyFrame
        this.secondaryStatus = secondaryStatus
        this.typeName = typeName
        this.online = online
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
        return result
    }

    override fun toString(): String {
        return "platform=$platform,nickname=$nickname,roomId=$roomId,showId=$showId,otherParams=$otherParams"
    }
}
