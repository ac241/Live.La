package com.acel.livela.main

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.acel.livela.R

class MainActivity : AppCompatActivity(), MainConstract.View {
    val MainConstract.Presenter = null
    override fun setPresenter(presenter: MainConstract.Presenter) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }
}
