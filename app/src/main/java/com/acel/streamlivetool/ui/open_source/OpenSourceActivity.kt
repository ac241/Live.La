package com.acel.streamlivetool.ui.open_source

import android.content.Intent
import android.graphics.Color
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.ui.splash.SplashActivity
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.activity_open_source.*
import kotlin.random.Random

class OpenSourceActivity : BaseActivity() {
    override fun getResLayoutId(): Int {
        return R.layout.activity_open_source
    }

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

    override fun createDo() {
        open_source_title.setOnClickListener {
//            setTitleColor()
            titileAnimate()
            if (titleClickTimes >= if (fullVersion) 10 else 100) {
                defaultSharedPreferences.edit()
                    .putBoolean(resources.getString(R.string.full_version), !fullVersion).apply()
                toast("full version ${!fullVersion}")
                MyApplication.finishAllActivity()
                startActivity(Intent(this, SplashActivity::class.java))
            } else
                titleClickTimes++
        }
        val list = mutableListOf<Module>()
        list.add(
            Module(
                "retrofit",
                "com.squareup.retrofit2",
                "https://github.com/square/retrofit",
                "Copyright 2013 Square, Inc.",
                "Licensed under the Apache License, Version 2.0 "
            )
        )
        list.add(
            Module(
                "gson",
                "com.google.code.gson",
                "https://github.com/google/gson",
                "Copyright 2008 Google Inc.",
                "Licensed under the Apache License, Version 2.0 "
            )
        )
        list.add(
            Module(
                "glide",
                "com.github.bumptech.glide",
                "https://github.com/bumptech/glide",
                "bumptech",
                "BSD, part MIT and Apache 2.0"
            )
        )
        list.add(
            Module(
                "RoundedImageView",
                "com.makeramen:roundedimageview",
                "https://github.com/vinc3m1/RoundedImageView",
                "Vincent Mi",
                "Licensed under the Apache License, Version 2.0"
            )
        )
        list.add(
            Module(
                "rhino:1.7.9",
                "libs/rhino-1.7.9.jar",
                "https://github.com/mozilla/rhino",
                "Mozilla",
                "licensed under the Mozilla Public License 2.0",
                true
            )
        )
        list.add(
            Module(
                "exoplayer:2.11.3",
                "com.google.android.exoplayer",
                "https://github.com/google/ExoPlayer",
                "Copyright 2008 Google Inc.",
                "Apache License Version 2.0, January 2004",
                true
            )
        )

        list.forEach {
            addModuleToTextView(it)
        }

        open_source_text.text = stringBuilder.toString()
    }

    private fun titileAnimate() {
        val randomFloat = Random.nextDouble(0.99, 1.01)
        open_source_title.animate().setDuration(100).scaleX(randomFloat.toFloat())
            .scaleY(randomFloat.toFloat()).start()
    }

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
