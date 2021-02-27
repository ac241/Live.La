package com.acel.streamlivetool.ui.custom_view

import android.content.Context
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication

fun blackAlphaPopupMenu(context: Context, view: View): PopupMenu {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PopupMenu(
            context,
            view,
            Gravity.START,
            0,
            R.style.popup_menu_style_black_alpha
        )
    } else {
        PopupMenu(context, view)
    }
}

fun Menu.addItemWhiteTextColor(title: String?): MenuItem {
    return add(title).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val ss = SpannableString(title)
            val foregroundColorSpan =
                ForegroundColorSpan(MyApplication.application.getColor(R.color.alphaWhite))
            ss.setSpan(
                foregroundColorSpan, 0,
                ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            this.title = ss
        }
    }
}
