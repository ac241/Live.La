package com.acel.streamlivetool.ui.custom

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.acel.streamlivetool.R

object AlertDialogTool {
    fun newAlertDialog(context: Context): AlertDialog.Builder {
        return AlertDialog.Builder(context)
    }
}