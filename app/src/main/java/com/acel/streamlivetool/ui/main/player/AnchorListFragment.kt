package com.acel.streamlivetool.ui.main.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.acel.streamlivetool.base.BaseFragment
import com.acel.streamlivetool.databinding.FragmentPlayerAnchorListBinding

class AnchorListFragment : BaseFragment() {
    private val viewModel by activityViewModels<PlayerViewModel>()
    private lateinit var binding: FragmentPlayerAnchorListBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlayerAnchorListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.listView.apply {
            adapter = viewModel.anchorList.value?.let {
                PlayerListAdapter(requireContext(), viewModel, it)
            }
        }
        viewModel.apply {
            anchorList.observe(viewLifecycleOwner) {
                binding.listView.adapter?.notifyDataSetChanged()
            }
            anchorPosition.observe(viewLifecycleOwner) {
                binding.listView.adapter?.apply {
                    this as PlayerListAdapter
                    setChecked(it)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AnchorListFragment()
    }
}