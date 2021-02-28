/*
 * Copyright (c) 2020.
 * @author acel
 */

package com.acel.streamlivetool.ui.main.add_anchor

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.net.ImageLoader
import kotlinx.android.synthetic.main.item_search_anchor.view.*

class SearchListAdapter(val anchorList: List<Anchor>) : BaseAdapter() {
    private var checkItem: Int? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = View.inflate(MyApplication.application, R.layout.item_search_anchor, null)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = convertView.tag as ViewHolder
        }

        anchorList[position].avatar?.apply {
            ImageLoader.load(
                MyApplication.application,
                this,
                viewHolder.avatar
            )
        }
        viewHolder.name.text = anchorList[position].nickname
        convertView?.tag = viewHolder
        if (checkItem == position)
            viewHolder.checked.visibility = View.VISIBLE
        else
            viewHolder.checked.visibility = View.GONE
        return view
    }

    override fun getItem(p0: Int): Anchor {
        return anchorList[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int = anchorList.size
    fun setChecked(which: Int) {
        checkItem = which
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) {
        val checked: ImageView = view.checked
        val avatar: ImageView = view.avatar
        val name: TextView = view.nickname
    }
}