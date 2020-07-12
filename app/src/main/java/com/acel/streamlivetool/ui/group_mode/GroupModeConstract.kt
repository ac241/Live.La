package com.acel.streamlivetool.ui.group_mode

import com.acel.streamlivetool.base.BasePresenter
import com.acel.streamlivetool.base.BaseView
import com.acel.streamlivetool.bean.Anchor

interface GroupModeConstract {
    interface Presenter : BasePresenter {
        fun addAnchor(queryAnchor: Anchor)
        fun getAnchorsStatus(anchor: Anchor)
        fun getAllAnchorsStatus()
    }

    interface View : BaseView<Presenter> {
        fun addAnchorSuccess(anchor: Anchor)
        fun addAnchorFailed(reason: String)
        fun refreshAnchorList()
        fun refreshAnchorStatus()
    }
}