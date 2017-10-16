package com.rapsealk.mobilesw.util

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask

/**
 * Created by rapsealk on 2017. 10. 4..
 */
class ProgressDialog : AsyncTask<Int, String, Int> {

    private var context: Context
    private var progressDialog: ProgressDialog
    private var message: String

    constructor(context: Context, message: String): super() {
        this.context = context
        this.progressDialog = ProgressDialog(context)
        this.message = message
    }

    override fun onPreExecute() {

        // progressDialog = ProgressDialog(context)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage(message)
        progressDialog.show()

        super.onPreExecute()
    }

    override fun doInBackground(vararg params: Int?): Int? {

        val taskCount = params[0]

        return taskCount
    }

    override fun onCancelled() { }

}