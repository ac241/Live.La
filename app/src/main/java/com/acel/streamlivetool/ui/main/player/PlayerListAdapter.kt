package com.acel.streamlivetool.ui.main.player

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.net.ImageLoader
import com.acel.streamlivetool.platform.PlatformDispatcher
import kotlinx.android.synthetic.main.item_player_list.view.*

class PlayerListAdapter(
    private val context: Context,
    private val viewModel: PlayerViewModel,
    private val list: List<Anchor>
) : RecyclerView.Adapter<PlayerListAdapter.ViewHolder>() {

    companion object {
        const val TYPE_LIVING = 1
        const val TYPE_NOT_LIVING = 2
    }

    private var nowCheckedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            TYPE_LIVING -> ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_player_list, parent, false)
            )
            else -> ViewHolder(
                LayoutInflater.from(context)
                    .inflate(R.layout.item_player_list_not_living, parent, false)
            )
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (nowCheckedPosition == position) {
            holder.isPlaying?.visibility = View.VISIBLE
        } else
            holder.isPlaying?.visibility = View.GONE
//        holder.itemView.setBackgroundColor(if (list[position].status) colorNormal else colorUnLiving)

        holder.nickname?.apply {
            text = list[position].nickname
            setOnClickListener { viewModel.playInList(position) }
        }
        list[position].avatar?.let {
            holder.avatar?.let { it1 -> ImageLoader.load(context, it, it1) }
        }
        PlatformDispatcher.getPlatformImpl(list[position])?.iconRes?.let {
            holder.icon?.setImageResource(it)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].status) TYPE_LIVING else TYPE_NOT_LIVING
    }

    override fun getItemCount(): Int = list.size

    fun setChecked(it: Int) {
        nowCheckedPosition = it
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nickname: TextView? = itemView.controller_nickname
        val avatar: ImageView? = itemView.avatar
        val icon: ImageView? = itemView.platform_icon
        val isPlaying: ImageView? = itemView.is_playing
    }
}