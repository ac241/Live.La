package com.acel.streamlivetool.ui.open_source

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.util.AppUtil.restartApp
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
            "licensed under the Mozilla Public License 2.0",
            true
        ),
        Module(
            "exoplayer:2.11.3",
            "com.google.android.exoplayer",
            "https://github.com/google/ExoPlayer",
            "Copyright 2008 Google Inc.",
            "Apache License Version 2.0, January 2004",
            true
        ),
        Module(
            "PermissionsDispatcher:4.7.0",
            "org.permissionsdispatcher",
            "https://github.com/permissions-dispatcher/PermissionsDispatcher",
            "Copyright 2016 Shintaro Katafuchi, Marcel Schnelle, Yoshinori Isogai",
            "Licensed under the Apache License, Version 2.0 (the \"License\")",
            false
        ),
        Module(
            "org.jsoup:jsoup:1.13.1",
            "org.jsoup",
            "https://jsoup.org/",
            "Jonathan Hedley",
            "The MIT License",
            false
        ),
        Module(
            "图标",
            "iconfont.cn",
            "https://www.iconfont.cn/user/detail?uid=133781",
            "搞设计的搬运工",
            "本app仅用于非商业用途"
        )
    )
    private val fullVersionClickTimes = 234
    private val fullVersion = defaultSharedPreferences.getBoolean(
        MyApplication.application.getString(R.string.full_version),
        false
    )
    private val stringBuilder = StringBuilder()
    private var titleClickTimes = 0
    private val colors =
        arrayOf(
            "#d50000", "#c51162", "#aa00ff", "#6200ea", "#304ffe", "#2962ff", "#0091ea",
            "#00b8d4", "#00bfa5", "#00c853", "#64dd17", "#aeea00", "#ffd600", "#ffab00",
            "#ff6d00", "#dd2c00", "#3e2723", "#212121", "#263238"
        )
    private var colorIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_source)
        createDo()
    }

    private fun createDo() {
        open_source_title.setOnClickListener {
//            setTitleColor()
            titleAnimate()
            if (titleClickTimes >= if (fullVersion) 10 else fullVersionClickTimes) {
                defaultSharedPreferences.edit()
                    .putBoolean(resources.getString(R.string.full_version), !fullVersion).apply()
                toast("Full version ${!fullVersion}")
                restartApp()
//                startActivity(Intent(this, MainActivity::class.java))
            } else
                titleClickTimes++
        }



        list.forEach {
            addModuleToTextView(it)
        }

        open_source_text.text = stringBuilder.toString()
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
        if (fullVersion || (!fullVersion && !module.hideWhenNotFullVersion))
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
