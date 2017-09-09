package com.rapsealk.mobilesw

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageButtonWorldPhoto.setOnClickListener { view: View ->
            // toast("WorldPhoto")
            var intent = Intent(this, WorldPhotoActivity::class.java)
            startActivity(intent)
        }

        imageButtonSetting.setOnClickListener { view: View ->
            toast("Setting")
        }

        imageButtonMyPage.setOnClickListener { view: View ->
            toast("MyPage")
        }

        imageButtonInfo.setOnClickListener { view: View ->
            // toast("Info")
            var intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

    }
}

// Extension
fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}