package com.acel.streamlivetool.ui.player


import android.content.Context
import android.net.ConnectivityManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformPitcher
import kotlinx.android.synthetic.main.activity_player.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread


class PlayerActivity : BaseActivity() {
    override fun getResLayoutId(): Int {
        return R.layout.activity_player
    }

    override fun init() {
        checkNetwork()
//        startPlay()
    }

    //网络提示
    private fun checkNetwork() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
//        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
//        Log.d("ACEL_LOG1", activityNetworkInfo.toString())
//        val
        if (activeNetworkInfo == null) {
            toast("没有网络")
            return
        }
//        if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI)
//            toast("Wifi网络")
        if (activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE)
            toast("您正在使用移动数据流量！")
//        Log.d("ACEL_LOG2", "z" + activityNetworkInfo.subtypeName)
    }

//    private fun startPlay() {
//        val anchor = intent.getParcelableExtra<Anchor>("anchor")
//        val platformImpl = PlatformPitcher.getPlatformImpl(anchor.platform)
//        val title = platformImpl?.platformShowNameRes?.let { getString(it) } + " " + anchor.nickname
//        doAsync {
//            var path = platformImpl?.getStreamingLiveUrl(anchor)
//            uiThread {
//                if (path == null) {
//                    path = ""
//                    toast("获取直播链接失败")
//                }
//                player.setUrl(path)
//                val controller = MyVideoController(this@PlayerActivity)
//                controller.setTitle(title) //设置视频标题
//                player.setVideoController(controller) //设置控制器，如需定制可继承BaseVideoController
//                player.start()
////                } else
//            }
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        player.pause()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        player.resume()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        player.release()
//    }
//
//
//    override fun onBackPressed() {
//        if (!player.onBackPressed()) {
//            super.onBackPressed()
//        }
//    }

}
