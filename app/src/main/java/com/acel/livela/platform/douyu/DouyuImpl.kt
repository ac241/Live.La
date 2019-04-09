package com.acel.livela.platform.douyu

import com.acel.livela.bean.Anchor
import com.acel.livela.platform.IPlatform
import com.acel.livela.util.TextUtil

object DouyuImpl : IPlatform {

    override val platform: String = "douyu"

    override fun getStatus() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getStreamingLive() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun startAppById() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAnchor(): Anchor {
        val douyuService = retrofit.create(DouyuNetApi::class.java)
        val html = douyuService.getRoomInfo("78622").execute().body().toString()
        val nickname = TextUtil.subString(html, "\"nickname\":\"", "\",")
        val showId = TextUtil.subString(html, "\"rid\":", ",\"")
        return Anchor(platform, showId, nickname, showId)
    }
}