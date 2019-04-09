package com.acel.livela.main

import com.acel.livela.BasePresenter
import com.acel.livela.BaseView

interface MainConstract {
    interface Presenter : BasePresenter {
        fun getAnchorsStatus()
    }

    interface View : BaseView<Presenter>{
        fun refreshAnchorStatus()
    }
}