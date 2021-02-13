/*
 * Copyright (c) 2020.
 * author acel
 */

package com.acel.streamlivetool.util

import android.widget.Toast
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.util.AppUtil.mainThread

object ToastUtil {
    private val context by lazy { MyApplication.application.applicationContext }

    fun toast(string: String) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }

    fun longToast(string: String) {
        Toast.makeText(context, string, Toast.LENGTH_LONG).show()
    }

    fun toastOnMainThread(string: String){
        mainThread { toast(string)}
    }
}