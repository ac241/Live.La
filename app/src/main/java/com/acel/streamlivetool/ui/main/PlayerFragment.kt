package com.acel.streamlivetool.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.databinding.ActivityPlayerBinding
import com.acel.streamlivetool.net.ImageLoader.loadImage

class PlayerFragment : Fragment() {
    private lateinit var binding: ActivityPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            anchor = it.getParcelable("anchor")
            anchorList = it.getParcelableArrayList("anchor_list")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        anchor?.avatar?.let {
            binding.avatar.loadImage(it)

        }
    }


    companion object {
        private val playerFragment by lazy { PlayerFragment() }
        private var anchor: Anchor? = null
        private var anchorList: ArrayList<Anchor>? = null

        @JvmStatic
        fun getInstance() = playerFragment

        //            PlayerFragment().apply {
//                BlankFragment().apply {
//                    arguments = Bundle().apply {
//                        putParcelable("anchor", anchor)
//                        putParcelableArrayList("anchor_list", anchorList)
//                    }
//                }
//            }
        @JvmStatic
        fun setAnchorData(anchor: Anchor, anchorList: ArrayList<Anchor>?) {
            this.anchor = anchor
            this.anchorList = anchorList
        }
    }
}