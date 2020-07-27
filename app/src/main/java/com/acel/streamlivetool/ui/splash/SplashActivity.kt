package com.acel.streamlivetool.ui.splash

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity

class SplashActivity : BaseActivity() {
    override fun getResLayoutId(): Int {
        return R.layout.activity_splash
    }

    override fun createdDo() {
        finish()
    }
}