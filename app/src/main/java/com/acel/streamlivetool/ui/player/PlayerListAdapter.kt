package com.acel.streamlivetool.ui.player

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.net.ImageLoader
import com.acel.streamlivetool.platform.PlatformDispatcher
import kotlinx.android.synthetic.main.item_player_list.view.*

class PlayerListAdapter(
    private val playerActivity: PlayerActivity,
    private val list: List<Anchor>
) :
    RecyclerView.Adapter<PlayerListAdapter.ViewHolder>() {

    private var nowCheckedPosition = -1

    private val colorNormal = Color.parseColor("#FFFFFF")
    private val colorUnLiving = Color.parseColor("#E3E3E3")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(playerActivity).inflate(R.layout.item_player_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (nowCheckedPosition == position) {
            holder.isPlaying?.visibility = View.VISIBLE
        } else
            holder.isPlaying?.visibility = View.GONE
        holder.itemView.setBackgroundColor(if (list[position].status) colorNormal else colorUnLiving)

        holder.nickname?.apply {
            text = list[position].nickname
            setOnClickListener { playerActivity.viewModel.playInList(position) }
        }
        list[position].avatar?.let {
            holder.avatar?.let { it1 -> ImageLoader.load(playerActivity, it, it1) }
        }
        PlatformDispatcher.getPlatformImpl(list[position])?.iconRes?.let {
            holder.icon?.setImageResource(it)
        }

    }

    override fun getItemCount(): Int = list.size

    fun setChecked(it: Int) {
        nowCheckedPosition = it
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nickname: TextView? = itemView.nickname
        val avatar: ImageView? = itemView.avatar
        val icon: ImageView? = itemView.platform_icon
        val isPlaying: ImageView? = itemView.is_playing
    }


}