package com.acel.livela.main

import android.support.v7.widget.LinearLayoutManager
import com.acel.livela.R
import com.acel.livela.base.BaseActivity
import com.acel.livela.bean.Anchor
import com.acel.livela.platform.PlatformPitcher
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info

class MainActivity : BaseActivity(), MainConstract.View, AnkoLogger {
    val presenter = MainPresenter(this)
    val mutableList = mutableListOf<Anchor>()

    override fun getResLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun init() {
        presenter.test()
        main_recycler_view.layoutManager = LinearLayoutManager(this)
//        main_recycler_view.adapter = RecyclerView

        //test
        mutableList.add(Anchor("douyu", "78622", "二珂", "78622"))

        doAsync {
            mutableList.forEach {
                val platformImpl = PlatformPitcher.getPlatformImpl(it.platform)
                val anchorInfo = platformImpl?.getAnchor()
                anchorInfo?.let {
                    info {
                        it.nickname + "/" + it.showId + "/" + it.roomId
                    }
                }
            }

        }

        btn_test.setOnClickListener {

        }
    }


    override fun refreshAnchorStatus() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
