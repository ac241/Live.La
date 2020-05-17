package com.acel.streamlivetool.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getResLayoutId())
        init()
    }

    open fun init() {

    }

    abstract fun getResLayoutId(): Int
}