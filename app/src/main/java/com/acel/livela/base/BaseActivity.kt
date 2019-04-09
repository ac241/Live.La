package com.acel.livela.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

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