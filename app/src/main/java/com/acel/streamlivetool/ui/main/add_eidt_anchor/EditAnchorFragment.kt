package com.acel.streamlivetool.ui.main.add_eidt_anchor

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.databinding.FragmentEditAnchorBinding
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.PlatformDispatcher.platformImpl
import com.acel.streamlivetool.util.ToastUtil.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditAnchorFragment : BottomSheetDialogFragment() {
    lateinit var binding: FragmentEditAnchorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AddAnchorFragmentStyle)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditAnchorBinding.inflate(inflater)
        return binding.root
    }

    var newestData: Anchor? = null

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newestData = null
        editAnchor?.apply {
            val platformImpl = platformImpl()
            binding.title.text = "修改 $nickname"
            binding.nickname.setText(nickname)
            binding.platformAndId.text = "${platformImpl?.platformName} $showId"
            binding.getNewestAnchorData.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    runCatching {
                        val newData = platformImpl?.getAnchor(this@apply)
                        newData?.let {
                            withContext(Dispatchers.Main) {
                                binding.nickname.setText(it.nickname)
                                binding.platformAndId.text =
                                    "${platformImpl.platformName} ${it.showId}"
                                toast("获取数据成功")
                                newestData = newData
                            }
                        }
                    }.onFailure {
                        it.printStackTrace()
                        withContext(Dispatchers.Main) {
                            toast("发生错误")
                        }
                    }
                }
            }
            binding.confirm.setOnClickListener {
                if (binding.nickname.text.toString().isEmpty()) {
                    binding.nickname.error = getString(R.string.please_fill_anchor_name)
                    return@setOnClickListener
                }
                editAnchor?.apply {
                    nickname = binding.nickname.text.toString()
                    newestData?.let {
                        roomId = it.roomId
                        showId = it.showId
                        otherParams = it.otherParams
                    }
                }?.let { it1 ->
                    AnchorRepository.getInstance().updateAnchor(it1)
                    toast("更新成功")
                    dismiss()
                }
            }
        }
    }

    companion object {
        private val instance by lazy { EditAnchorFragment() }
        private var editAnchor: Anchor? = null

        @JvmStatic
        fun getInstance(anchor: Anchor): EditAnchorFragment {
            editAnchor = anchor
            return instance
        }
//            EditAnchorFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
    }
}