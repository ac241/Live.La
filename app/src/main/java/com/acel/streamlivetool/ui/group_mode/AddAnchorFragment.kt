package com.acel.streamlivetool.ui.group_mode

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import com.acel.streamlivetool.R
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.ToastUtil.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_add_anchor.*


class AddAnchorFragment : BottomSheetDialogFragment() {
    private lateinit var platformList: List<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AddAnchorFragmentStyle)
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
//        edit_anchor_id_add_anchor.requestFocus()
//        edit_anchor_id_add_anchor.selectAll()

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
            val groupModeActivity = activity as GroupModeActivity
            groupModeActivity.presenter.addAnchor(Anchor(platform, "", roomId, ""))
        }
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) {
            imm.hideSoftInputFromWindow(activity?.window?.decorView?.applicationWindowToken, 0)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        hideKeyboard()
        super.onDismiss(dialog)
    }

    fun onGetAnchorInfoSuccess(anchor: Anchor) {
        runOnUiThread {
            toast("添加成功${anchor.nickname}")
        }
    }

    fun onGetAnchorInfoFailed(reason: String) {
        runOnUiThread {
            toast("添加失败：$reason")
        }
    }
}
