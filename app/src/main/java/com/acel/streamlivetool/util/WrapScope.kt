package com.acel.streamlivetool.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun CoroutineScope.launchIO(block: suspend CoroutineScope.() -> Unit, failed: (suspend CoroutineScope.(Throwable) -> Unit)? = null) {
    launch(Dispatchers.IO) {
        runCatching {
            block()
        }.onFailure {
            it.printStackTrace()
            failed?.invoke(this, it)
        }
    }
}