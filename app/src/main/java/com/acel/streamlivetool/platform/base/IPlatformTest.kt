package com.acel.streamlivetool.platform.base

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.CallSuper
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.Result
import com.acel.streamlivetool.bean.StreamingLive
import com.acel.streamlivetool.net.RetrofitUtils
import com.acel.streamlivetool.platform.bean.ResultGetAnchorListByCookieMode
import com.acel.streamlivetool.ui.main.player.DanmuManager
import com.acel.streamlivetool.util.AppUtil.mainThread
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences
import retrofit2.Retrofit

interface IPlatformTest {


}