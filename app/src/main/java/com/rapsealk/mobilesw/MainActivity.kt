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
            this.onPause()
            startActivity(intent)
        }

        imageButtonSetting.setOnClickListener { view: View ->
            toast("Setting")
        }

        imageButtonMyPage.setOnClickListener { view: View ->
            // toast("MyPage")
            var intent = Intent(this, LoginActivity::class.java)
            this.onPause()
            startActivity(intent)
        }

        imageButtonInfo.setOnClickListener { view: View ->
            // toast("Info")
            var intent = Intent(this, InfoActivity::class.java)
            this.onPause()
            startActivity(intent)
        }

    }
}

// Extension
fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}