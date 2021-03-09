package com.acel.streamlivetool.ui.custom

import android.app.AlertDialog
import android.content.Context
import com.acel.streamlivetool.R

object AlertDialogTool {
    fun newAlertDialog(context: Context): AlertDialog.Builder {
        return AlertDialog.Builder(context, R.style.default_dialog)
    }
}