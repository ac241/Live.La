package com.acel.streamlivetool.ui.main.add_anchor

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.util.AppUtil.mainThread
import com.acel.streamlivetool.util.ToastUtil.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_add_anchor.*


class AddAnchorFragment : BottomSheetDialogFragment() {
    private lateinit var platformList: List<String>
    private val viewModel by viewModels<AddAnchorViewModel> {
        AddAnchorViewModel.ViewModeFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AddAnchorFragmentStyle)
//        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        viewModel.apply {
            liveDataResultSuccess.observe(this@AddAnchorFragment, {
                it?.let {
                    (requireActivity() as MainActivity).checkFollowed(it)
                    addAnchorSuccess(it)
                }
            })
            liveDataResultFailed.observe(this@AddAnchorFragment, {
                it?.let { addAnchorFailed(it) }
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_anchor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        platformList = PlatformDispatcher.getAllPlatform()
        val tempList = mutableListOf<String>()
        //显示radio
        platformList.forEachIndexed { i, s ->
            val chop = Chip(requireContext())
            chop.isCheckable = true
            val stringList = s.split(",")
            tempList.add(stringList[0])
            chop.text = stringList[1]
            chop.id = i
            chip_group_add_anchor.addView(chop)
        }
        chip_group_add_anchor.isSingleSelection = true
        btn_confirm_add_anchor.setOnClickListener {
            val roomId = edit_anchor_id_add_anchor.text.toString()
            roomId.ifEmpty {
                edit_anchor_id_add_anchor.error = "直播间Id不能为空"
                return@setOnClickListener
            }
            val radioIndex = chip_group_add_anchor.checkedChipId
            if (radioIndex == -1) {
                toast("请选择平台")
                return@setOnClickListener
            }
            val platform = tempList[radioIndex]

            viewModel.addAnchor(Anchor(platform, "", roomId, ""))
        }
        btn_search_anchor.setOnClickListener {
            val keyword = edit_anchor_id_add_anchor.text.toString()
            keyword.ifEmpty {
                edit_anchor_id_add_anchor.error = "搜索关键词不能为空"
                return@setOnClickListener
            }
            val radioIndex = chip_group_add_anchor.checkedChipId
            if (radioIndex == -1) {
                toast("请选择平台")
                return@setOnClickListener
            }
            val platform = tempList[radioIndex]
            viewModel.search(requireActivity(), keyword, platform)
        }
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(dialog?.window?.decorView?.applicationWindowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.restoreLiveData()
        hideKeyboard()
    }

    private fun addAnchorFailed(reason: String) {
        mainThread {
            toast("添加失败：$reason")
        }
    }

    private fun addAnchorSuccess(anchor: Anchor) {
        mainThread {
            toast("添加成功${anchor.nickname}")
            (requireActivity() as MainActivity).gotoMainPage()
        }
    }

    companion object {
        val instance by lazy { AddAnchorFragment() }
    }

}
