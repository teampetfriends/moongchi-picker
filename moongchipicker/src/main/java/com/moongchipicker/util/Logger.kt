package com.moongchipicker.util

import android.util.Log

internal object Logger {

    fun d(msg : String){
        Log.d("moonchipicker", msg)
    }

    fun e(msg : String){
        Log.e("moonchipicker", msg)
    }
}