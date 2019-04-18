package com.acel.livela.ui.main

import com.acel.livela.BasePresenter
import com.acel.livela.BaseView
import com.acel.livela.bean.Anchor

interface MainConstract {
    interface Presenter : BasePresenter {
        fun addAnchor(queryAnchor: Anchor)
        fun getAnchorsStatus()
        fun startPlay(anchor: Anchor)
        fun deleteAnchor(queryAnchor: Anchor)
    }

    interface View : BaseView<Presenter> {
        fun addAnchorSuccess(anchor: Anchor)
        fun addAnchorFail(reason: String)
        fun refreshAnchorList()
        fun refreshAnchorStatus(anchor: Anchor)
    }
}