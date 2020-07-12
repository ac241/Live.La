package com.acel.streamlivetool.ui.open_source

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.activity_open_source.*

class OpenSourceActivity : BaseActivity() {
    override fun getResLayoutId(): Int {
        return R.layout.activity_open_source
    }

    private val stringBuilder = StringBuilder()
    private var titleClickTimes = 0
    private var setFullVersion = false
    override fun init() {
        open_source_title.setOnClickListener {

            if (titleClickTimes >= 100) {
                if (!setFullVersion) {
                    toast("ok")
                    defaultSharedPreferences.edit()
                        .putBoolean(resources.getString(R.string.full_version), true).apply()
                    setFullVersion = true
                }
            } else {
                titleClickTimes++
            }

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
//        list.add(
//            Module(
//                "rhino:1.7.9",
//                "libs/rhino-1.7.9.jar",
//                "https://github.com/mozilla/rhino",
//                "Mozilla",
//                "licensed under the Mozilla Public License 2.0"
//            )
//        )


        list.forEach {
            addModuleToTextView(it)
        }

        open_source_text.text = stringBuilder.toString()
    }

    private fun addModuleToTextView(module: Module) {
        stringBuilder.append("--------------------\n")
        stringBuilder.append(" " + module.name + "\n")
        stringBuilder.append("--------------------\n")
        stringBuilder.append("  " + module.path + "\n")
        stringBuilder.append("  " + module.author + "\n")
        stringBuilder.append("  " + module.licensed + "\n\n")
    }

}
