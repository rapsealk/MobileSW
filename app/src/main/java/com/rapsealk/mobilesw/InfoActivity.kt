package com.rapsealk.mobilesw

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_info.*

class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        buttonBackToMain.setOnClickListener { view ->
            finish()    // Remove current activity
        }
    }
}
