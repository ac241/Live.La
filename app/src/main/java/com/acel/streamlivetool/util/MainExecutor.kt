package com.acel.streamlivetool.util

import android.util.Log
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.ToastUtil.toast
import java.lang.Exception
import java.util.concurrent.Executors

object MainExecutor {
    private val poolExecutor = Executors.newFixedThreadPool(20)
    fun execute(method: () -> Unit) {
        poolExecutor.execute {
            try {
                method.invoke()
            } catch (e: Exception) {
                runOnUiThread {
                    toast(e.javaClass.name)
                }
                e.printStackTrace()
            }
        }
    }

    fun execute(runnable: Runnable) {
        try {
            poolExecutor.execute(runnable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}