package com.acel.streamlivetool.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getResLayoutId())
        MyApplication.addActivityToManageList(this)
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        MyApplication.removeActivityFromManageList(this)
    }

    abstract fun getResLayoutId(): Int

    open fun init() {

    }
}