package com.acel.streamlivetool.platform.impl.douyu.module

import android.content.Context
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.base.AnchorCookieModule
import com.acel.streamlivetool.platform.base.CookieManager
import com.acel.streamlivetool.platform.bean.ApiResult
import com.acel.streamlivetool.platform.impl.douyu.DouyuImpl
import com.acel.streamlivetool.platform.impl.douyu.bean.Followed
import com.acel.streamlivetool.util.CookieUtil
import com.acel.streamlivetool.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class DouyuAnchorCookieModule(private val platform: String, cookieManager: CookieManager) :
    AnchorCookieModule(cookieManager) {
    override fun getAnchorsByCookieMode(): ApiResult<List<Anchor>> {
        fun addToList(list: MutableList<Anchor>, followed: Followed) {
            followed.data.list.forEach {
                list.add(
                    Anchor(
                        platform = platform,
                        nickname = it.nickname,
                        showId = it.room_id.toString(),
                        roomId = it.room_id.toString(),
                        status = it.show_status == 1,
                        title = it.room_name,
                        avatar = it.avatar_small,
                        keyFrame = it.room_src,
                        secondaryStatus = if (it.videoLoop == 1) MyApplication.application.getString(
                            R.string.video_looping
                        ) else null,
                        typeName = it.game_name,
                        online = it.online,
                        liveTime = TimeUtil.timestampToString(it.show_time)
                    )
                )
            }
        }

        cookieManager.getCookie().run {
            if (this.isEmpty())
                return ApiResult(false, "cookie为空", cookieValid = false)
            else {
                val followed = DouyuImpl.douyuService.getFollowed(this).execute().body()
                if (followed?.error != 0)
                    return ApiResult(false, followed?.msg.toString(), cookieValid = false)
                else {
                    val list = mutableListOf<Anchor>()
                    addToList(list, followed)
                    //如果页数大于1
                    followed.data.pageCount.let { page ->
                        if (page > 1) {
                            runBlocking {
                                for (i in 2..followed.data.pageCount) {
                                    async(Dispatchers.IO) {
                                        val followedNext =
                                            DouyuImpl.douyuService.getFollowed(this@run, i)
                                                .execute().body()
                                        if (followedNext != null) {
                                            addToList(list, followedNext)
                                        }
                                    }.start()
                                }
                            }
                        }
                    }
                    return ApiResult(true, "", list)
                }
            }
        }
    }

    override val supportFollow: Boolean = true

    override fun follow(context: Context, anchor: Anchor): ApiResult<String> {
        cookieManager.getCookie().let { cookie ->
            if (cookie.isEmpty())
                return ApiResult(false, "未登录")
            val response = DouyuImpl.douyuService.initCsrf(cookie).execute()
            val setCookie = response.headers().get("Set-Cookie") ?: ""
            val ctn = CookieUtil.getCookieField(setCookie, "acf_ccn")
            ctn?.let { c ->
                val result =
                    DouyuImpl.douyuService.follow("$setCookie;$cookie", anchor.roomId, c).execute()
                        .body()
                result?.apply {
                    return if (error == 0)
                        ApiResult(true, "关注成功")
                    else
                        ApiResult(false, msg)
                }
            }
        }
        return ApiResult(false, "发生错误")
    }
}