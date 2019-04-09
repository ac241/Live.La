package com.acel.livela.main

import com.acel.livela.bean.Anchor
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class MainPresenter(var view: MainConstract.View) : MainConstract.Presenter, AnkoLogger {
    val anchorList = mutableListOf<Anchor>()

    init {
        //get anchors from database
    }

    override fun getAnchorsStatus() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun test() {
        info { "pre test" }
    }
}