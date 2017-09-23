package com.rapsealk.mobilesw.fragment

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rapsealk.mobilesw.R
import kotlinx.android.synthetic.main.fragment_post.*

/**
 * Created by rapsealk on 2017. 9. 23..
 */
class PostFragment : Fragment { // TODO https://developer.android.com/guide/components/fragments.html?hl=ko

    constructor () : super() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buttonClose.setOnClickListener { view ->
            super.onDestroyView()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // return super.onCreateView(inflater, container, savedInstanceState)
        return inflater!!.inflate(R.layout.fragment_post, container, false)
    }

    override fun onPause() {
        super.onPause()
    }

    /*
     * onAttach
     * onActivityCreated
     * onStart
     * onResume
     * onStop
     * onDestroyView
     * onDestroy
     * onDetach
     */
}