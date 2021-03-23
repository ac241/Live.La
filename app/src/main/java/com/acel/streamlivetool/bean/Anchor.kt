package com.acel.streamlivetool.bean

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.acel.streamlivetool.anchor_extension.action.AnchorExtensionInterface

@Entity
class Anchor() : Parcelable {
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

    /**
     * 直播时间
     */
    @Ignore
    var liveTime: String? = null

    /**
     * 扩展按钮功能
     */
    @Ignore
    var anchorExtensions: List<AnchorExtensionInterface>? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        platform = parcel.readString().toString()
        nickname = parcel.readString().toString()
        showId = parcel.readString().toString()
        roomId = parcel.readString().toString()
        otherParams = parcel.readString().toString()
        status = parcel.readByte() != 0.toByte()
        title = parcel.readString()
        avatar = parcel.readString()
        keyFrame = parcel.readString()
        secondaryStatus = parcel.readString()
        typeName = parcel.readString()
        online = parcel.readString()
        liveTime = parcel.readString()
    }

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
        online: String? = null,
        liveTime: String? = null
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
        this.liveTime = liveTime
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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(platform)
        parcel.writeString(nickname)
        parcel.writeString(showId)
        parcel.writeString(roomId)
        parcel.writeString(otherParams)
        parcel.writeByte(if (status) 1 else 0)
        parcel.writeString(title)
        parcel.writeString(avatar)
        parcel.writeString(keyFrame)
        parcel.writeString(secondaryStatus)
        parcel.writeString(typeName)
        parcel.writeString(online)
        parcel.writeString(liveTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Anchor> {
        override fun createFromParcel(parcel: Parcel): Anchor {
            return Anchor(parcel)
        }

        override fun newArray(size: Int): Array<Anchor?> {
            return arrayOfNulls(size)
        }
    }
}
