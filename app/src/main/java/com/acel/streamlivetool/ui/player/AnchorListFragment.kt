package com.acel.streamlivetool.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.acel.streamlivetool.R

/**
 * A fragment representing a list of Items.
 */
class AnchorListFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_danmu_list_list, container, false)
        return view
    }

    companion object {

        @JvmStatic
        fun newInstance() = AnchorListFragment()
    }
}