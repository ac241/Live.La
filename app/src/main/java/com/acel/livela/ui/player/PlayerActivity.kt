package com.acel.livela.ui.player


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.acel.livela.R
import com.acel.livela.base.BaseActivity
import com.acel.livela.bean.Anchor
import com.acel.livela.platform.PlatformPitcher
import kotlinx.android.synthetic.main.activity_player.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread


class PlayerActivity : BaseActivity() {
    override fun getResLayoutId(): Int {
        return R.layout.activity_player
    }

    lateinit var anchor: Anchor

    override fun init() {
        anchor = intent.getParcelableExtra<Anchor>("anchor")
        startPlay()
    }

    private fun startPlay() {
        val platformImpl = PlatformPitcher.getPlatformImpl(anchor.platform)
        val title = platformImpl?.platformShowNameRes?.let { getString(it) } + " " + anchor.nickname
        doAsync {
            val path = platformImpl?.getStreamingLiveUrl(anchor)
            uiThread {
                if (path != null) {
                    player.setUrl(path)
                    val controller = MyVideoController(this@PlayerActivity)
                    controller.setTitle(title) //设置视频标题
                    player.setVideoController(controller) //设置控制器，如需定制可继承BaseVideoController
                    player.start()
                } else
                    toast("获取直播链接失败")
            }

        }
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onResume() {
        super.onResume()
        player.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }


    override fun onBackPressed() {
        if (!player.onBackPressed()) {
            super.onBackPressed()
        }
    }

}
