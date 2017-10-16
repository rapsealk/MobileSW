package com.rapsealk.mobilesw.util

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import java.io.File

/**
 * Created by rapsealk on 2017. 10. 6..
 */
class MediaScanner : MediaScannerConnection.MediaScannerConnectionClient {

    private var mConnection: MediaScannerConnection? = null
    private var mTargetFile: File? = null

    constructor(context: Context, targetFile: File) {
        mTargetFile = targetFile
        mConnection = MediaScannerConnection(context, this)
        mConnection?.connect()
    }

    override fun onMediaScannerConnected() {
        mConnection?.scanFile(mTargetFile?.absolutePath, null)
    }

    override fun onScanCompleted(path: String?, uri: Uri?) {
        mConnection?.disconnect()
    }
}