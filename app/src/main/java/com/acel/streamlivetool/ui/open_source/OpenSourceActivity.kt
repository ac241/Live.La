package com.acel.streamlivetool.ui.open_source

import android.graphics.Color
import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.activity_open_source.*
import kotlin.random.Random

class OpenSourceActivity : AppCompatActivity() {
    val list = listOf(
        Module(
            "retrofit",
            "com.squareup.retrofit2",
            "https://github.com/square/retrofit",
            "Copyright 2013 Square, Inc.",
            "Licensed under the Apache License, Version 2.0 "
        ),
        Module(
            "gson",
            "com.google.code.gson",
            "https://github.com/google/gson",
            "Copyright 2008 Google Inc.",
            "Licensed under the Apache License, Version 2.0 "
        ),
        Module(
            "glide",
            "com.github.bumptech.glide",
            "https://github.com/bumptech/glide",
            "bumptech",
            "BSD, part MIT and Apache 2.0"
        ),
        Module(
            "RoundedImageView",
            "com.makeramen:roundedimageview",
            "https://github.com/vinc3m1/RoundedImageView",
            "Vincent Mi",
            "Licensed under the Apache License, Version 2.0"
        ),
        Module(
            "rhino:1.7.9",
            "libs/rhino-1.7.9.jar",
            "https://github.com/mozilla/rhino",
            "Mozilla",
            "licensed under the Mozilla Public License 2.0"
        ),
        Module(
            "exoplayer:2.11.3",
            "com.google.android.exoplayer",
            "https://github.com/google/ExoPlayer",
            "Copyright 2008 Google Inc.",
            "Apache License Version 2.0, January 2004"
        ),
        Module(
            "PermissionsDispatcher:4.7.0",
            "org.permissionsdispatcher",
            "https://github.com/permissions-dispatcher/PermissionsDispatcher",
            "Copyright 2016 Shintaro Katafuchi, Marcel Schnelle, Yoshinori Isogai",
            "Licensed under the Apache License, Version 2.0 (the \"License\")"
        ),
        Module(
            "jsoup:1.13.1",
            "org.jsoup",
            "https://jsoup.org/",
            "Jonathan Hedley",
            "The MIT License"
        ),
        Module(
            "图标",
            "iconfont.cn",
            "https://www.iconfont.cn/user/detail?uid=133781",
            "搞设计的搬运工",
            "本app仅用于非商业用途"
        )
    )
    private val stringBuilder = StringBuilder()
    private var titleClickTimes = 0
    private val colors =
        arrayOf(
            "#f16d7a", "#e27386", "#f55066", "#ef5464", "#ae716e", "#cb8e85", "#cf8878",
            "#c86f67", "#f1ccb8", "#f2debd", "#b7d28d", "#dcff93", "#ff9b6a", "#f1b8e4",
            "#d9b8f1", "#f1ccb8", "#f1f1b8", "#b8f1ed", "#b8f1cc", "#b8f1cc", "#e7dac9",
            "#e1622f", "#f3d64e", "#fd7d36", "#fe9778", "#c38e9e", "#f28860", "#de772c",
            "#e96a25", "#ca7497", "#e29e4b", "#edbf2b", "#fecf45", "#f9b747", "#c17e61",
            "#ed9678", "#ffe543", "#e37c5b", "#ff8240", "#aa5b71", "#f0b631", "#cf8888"
        )
    private var colorIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_source)
        createDo()
    }

    private fun createDo() {
        open_source_title.setOnClickListener {
            setTitleColor()
            titleAnimate()
            titleClickTimes++
        }

        list.forEach {
            addModuleToTextView(it)
        }
        module_text.text = stringBuilder.toString()
    }


    private fun titleAnimate() {
        val randomFloat = Random.nextDouble(0.99, 1.01)
        open_source_title.animate().setDuration(100).scaleX(randomFloat.toFloat())
            .scaleY(randomFloat.toFloat()).start()
    }

    @Suppress("unused")
    private fun setTitleColor() {
        open_source_title.setTextColor(Color.parseColor(colors[colorIndex++]))
        if (colorIndex > colors.size - 1)
            colorIndex = 0
    }

    private fun addModuleToTextView(module: Module) {
        add(module)
    }

    private fun add(module: Module) {
        stringBuilder.append("--------------------\n")
        stringBuilder.append(" " + module.name + "\n")
        stringBuilder.append("--------------------\n")
        stringBuilder.append("  " + module.path + "\n")
        stringBuilder.append("  " + module.author + "\n")
        stringBuilder.append("  " + module.licensed + "\n\n")
    }
}
