package com.acel.streamlivetool.ui.main

import android.content.Context
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.util.AnchorClickAction

object HandleContextItemSelect {
    fun handle(context: Context, itemId: Int, anchor: Anchor, list: List<Anchor>) {
        val array = arrayOf(
            R.id.action_item_open_as_open_app,
            R.id.action_item_open_as_inner_player,
            R.id.action_item_open_as_outer_player,
            R.id.action_item_open_as_overlay_player
        )
        if (itemId in array)
            AnchorClickAction.contextItemClick(context, anchor, list, itemId)
    }
}