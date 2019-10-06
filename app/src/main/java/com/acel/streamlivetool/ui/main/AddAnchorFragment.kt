package com.acel.streamlivetool.ui.main

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformPitcher
import kotlinx.android.synthetic.main.fragment_add_anchor.*
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.toast


class AddAnchorFragment : BottomSheetDialogFragment() {
    lateinit var platformList: List<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AddAnchorFragmentStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_anchor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        platformList = PlatformPitcher.getAllPlatfrom(this)
        val tempList = mutableListOf<String>()
        //显示radio
        platformList.forEachWithIndex { i, s ->
            val radioButton = RadioButton(context)
            val stringList = s.split(",")
            tempList.add(stringList[0])
            radioButton.setText(stringList[1])
            radioButton.id = i
            radio_group_add_anchor.addView(radioButton)
        }
        edit_anchor_id_add_anchor.requestFocus()

        btn_confirm_add_anchor.setOnClickListener {
            val roomId = edit_anchor_id_add_anchor.text.toString()
            roomId.ifEmpty {
                edit_anchor_id_add_anchor.setError("直播间Id不能为空")
                return@setOnClickListener
            }
            val radioIndex = radio_group_add_anchor.checkedRadioButtonId
            if (radioIndex == -1) {
                toast("请选择平台")
                return@setOnClickListener
            }
            val platform = tempList[radioIndex]
            val mainActivity = activity as MainActivity
            mainActivity.presenter.addAnchor(Anchor(platform, "", roomId, ""))
        }
    }

    fun hideKeyboard() {
        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(activity!!.getWindow().getDecorView().applicationWindowToken, 0)
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        hideKeyboard()
        super.onDismiss(dialog)
    }

    fun onGetAnchorInfoSuccess(anchor: Anchor) {
        runOnUiThread {
            toast("添加成功" + anchor.nickname)
        }
    }

    fun onGetAnchorInfoFailed(reason: String) {
        runOnUiThread {
            toast("添加失败：" + reason)
        }
    }
}
