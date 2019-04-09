package com.acel.livela

import android.support.design.widget.Snackbar
import com.acel.livela.base.BaseActivity
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : BaseActivity() {
    override fun getResLayoutId(): Int {
        return R.layout.activity_setting
    }

    override fun init() {
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

}
