package com.acel.streamlivetool.ui.open_source

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import kotlinx.android.synthetic.main.activity_open_source.*

class OpenSourceActivity : BaseActivity() {
    override fun getResLayoutId(): Int {
        return R.layout.activity_open_source
    }

    private val stringBuilder = StringBuilder()

    override fun init() {
        val list = mutableListOf<Module>()
        list.add(
            Module(
                "anko:0.10.8",
                "org.jetbrains.anko",
                "https://github.com/Kotlin/anko",
                "JetBrains inc.",
                "licensed under the Apache License 2.0"
            )
        )
        list.add(
            Module(
                "retrofit:2.5.0",
                "com.squareup.retrofit2",
                "https://github.com/square/retrofit",
                "Copyright 2013 Square, Inc.",
                "Licensed under the Apache License, Version 2.0 "
            )
        )
        list.add(
            Module(
                "gson:2.8.5",
                "com.google.code.gson",
                "https://github.com/google/gson",
                "Copyright 2008 Google Inc.",
                "Licensed under the Apache License, Version 2.0 "
            )
        )
        list.add(
            Module(
                "greendao:3.2.2",
                "org.greenrobot.greendao",
                "https://github.com/greenrobot/greenDAO",
                "greenrobot",
                "Licensed under the Apache License, Version 2.0 "
            )
        )
        list.add(
            Module(
                "rhino:1.7.9",
                "libs/rhino-1.7.9.jar",
                "https://github.com/mozilla/rhino",
                "Mozilla",
                "licensed under the Mozilla Public License 2.0"
            )
        )


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
