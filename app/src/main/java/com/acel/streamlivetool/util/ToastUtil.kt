/*
 * Copyright (c) 2020.
 * create on 20-5-13 下午10:52
 * author acel
 */

package com.acel.streamlivetool.util

import android.widget.Toast
import com.acel.streamlivetool.base.MyApplication

object ToastUtil {
    private var toast: Toast? = null
    private val context by lazy { MyApplication.application.applicationContext }


    fun toast(string: String) {
        if (toast == null)
            toast = Toast.makeText(context, string, Toast.LENGTH_SHORT)
        else {
            toast?.setText(string)
            toast?.duration = Toast.LENGTH_SHORT
        }
        toast?.show()
    }

    fun longToast(string: String) {
        if (toast == null)
            toast = Toast.makeText(context, string, Toast.LENGTH_LONG)
        else {
            toast?.setText(string)
            toast?.duration = Toast.LENGTH_LONG
        }
        toast?.show()
    }
}