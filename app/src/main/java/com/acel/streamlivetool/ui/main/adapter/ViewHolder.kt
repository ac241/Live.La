package com.acel.streamlivetool.ui.main.adapter

import android.view.ContextMenu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import kotlinx.android.synthetic.main.item_graphic_anchor.view.*
import kotlinx.android.synthetic.main.item_recycler_anchor.view.*


class ViewHolderStatusGroup(itemView: View) : RecyclerView.ViewHolder(itemView)

const val MODE_GROUP = 332
const val MODE_COOKIE = 333

class ViewHolderGraphic(itemView: View, private val modeType: Int) :
    RecyclerView.ViewHolder(itemView),
    View.OnCreateContextMenuListener {
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        when (modeType) {
            MODE_GROUP ->
                (itemView.context as AppCompatActivity).menuInflater.inflate(
                    R.menu.anchor_item_menu,
                    menu
                )
            MODE_COOKIE ->
                (itemView.context as AppCompatActivity).menuInflater.inflate(
                    R.menu.anchor_item_menu_cookie_mode,
                    menu
                )
        }
    }

    init {
        itemView.setOnCreateContextMenuListener(this)
    }

    val anchorName: TextView = itemView.grid_anchor_name
    val platform: TextView = itemView.grid_anchor_platform
    val image: ImageView = itemView.grid_anchor_image
    val avatar: ImageView = itemView.grid_anchor_avatar
    val status: TextView = itemView.grid_anchor_status
    val secondBtn: ImageView = itemView.grid_anchor_second_btn
    val title: TextView = itemView.grid_anchor_title
    val additionBtn: ImageView = itemView.grid_anchor_addition_action
}

class ViewHolderText(itemView: View, private val modeType: Int) :
    RecyclerView.ViewHolder(itemView),
    View.OnCreateContextMenuListener {
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        when (modeType) {
            MODE_GROUP ->
                (itemView.context as AppCompatActivity).menuInflater.inflate(
                    R.menu.anchor_item_menu,
                    menu
                )
            MODE_COOKIE ->
                (itemView.context as AppCompatActivity).menuInflater.inflate(
                    R.menu.anchor_item_menu_cookie_mode,
                    menu
                )
        }
    }

    init {
        itemView.setOnCreateContextMenuListener(this)
    }

    val anchorName: TextView = itemView.anchor_name
    val platform: TextView = itemView.main_anchor_platform
    val roomId: TextView = itemView.main_anchor_roomId
    val status: TextView = itemView.main_anchor_status
    val secondBtn: ImageView = itemView.main_second_btn
    val title: TextView = itemView.anchor_title
    val additionBtn: ImageView = itemView.btn_addition_action
}
