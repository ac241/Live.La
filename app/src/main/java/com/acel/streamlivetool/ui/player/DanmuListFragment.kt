package com.acel.streamlivetool.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.databinding.FragmentDanmuListListBinding

class DanmuListFragment : Fragment() {
    private val viewModel by activityViewModels<PlayerViewModel>()
    private lateinit var binding: FragmentDanmuListListBinding

    private var isLastDanmuVisiable = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDanmuListListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvDanmu.apply {
            adapter =
                viewModel.danmuList.value?.let { DanmuRecyclerViewAdapter(it) }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                val layoutManager = this@apply.layoutManager as LinearLayoutManager
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    isLastDanmuVisiable =
                        layoutManager.findLastVisibleItemPosition() == viewModel.danmuList.value!!.size - 1
                }
            })
        }
        viewModel.danmuList.observe(viewLifecycleOwner) {
            binding.rvDanmu.apply {
                adapter?.notifyDataSetChanged()
                if (isLastDanmuVisiable)
                    smoothScrollToPosition(it.size)
            }
        }
        viewModel.danmuStatus.observe(viewLifecycleOwner) {
            binding.notice.text = it.second
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = DanmuListFragment()
    }
}