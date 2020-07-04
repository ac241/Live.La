package com.acel.streamlivetool.ui.main

import com.acel.streamlivetool.base.BasePresenter
import com.acel.streamlivetool.base.BaseView
import com.acel.streamlivetool.bean.Anchor

interface MainConstract {
    interface Presenter : BasePresenter {
        fun addAnchor(queryAnchor: Anchor)
        fun getAnchorsStatus(anchor: Anchor)
        fun getAllAnchorsStatus()
        fun deleteAnchor(queryAnchor: Anchor)
    }

    interface View : BaseView<Presenter> {
        fun addAnchorSuccess(anchor: Anchor)
        fun addAnchorFailed(reason: String)
        fun refreshAnchorList()
        fun refreshAnchorStatus(anchor: Anchor)
    }
}