package com.acel.streamlivetool.base

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.acel.streamlivetool.base.MyApplication.Companion.finishAllActivity
import com.acel.streamlivetool.base.MyApplication.Companion.isActivityFirst

abstract class BaseActivity : AppCompatActivity() {

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getResLayoutId())
        MyApplication.addActivityToManageList(this)
        createdDo()
    }

    final override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    final override fun onDestroy() {
        super.onDestroy()
        MyApplication.removeActivityFromManageList(this)
        if (isActivityFirst(this))
            finishAllActivity()
        destroyDo()
    }

    open fun destroyDo() {

    }

    abstract fun getResLayoutId(): Int

    open fun createdDo() {

    }
}