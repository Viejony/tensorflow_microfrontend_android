package com.jamstudios.amf_example

import android.util.Log

object Utils {

    fun printLog(log: String) {
        Log.d("MICROFRONTEND", log)
    }

    fun printFloatArray(x: FloatArray, lineJump: Int? = null) {
        print("[")
        for(i in x.indices) {
            print("${x[i]}${ if(i != (x.size - 1)) "," else "" }${ if( lineJump != null && (i+1) % lineJump == 0) "\n" else "" }")
        }
        print("]\n")
    }

    fun printShortArray(x: ShortArray, lineJump: Int? = null) {
        print("[")
        for(i in x.indices) {
            print("${x[i]}${ if(i != (x.size - 1)) "," else "" }${ if( lineJump != null && (i+1) % lineJump == 0) "\n" else "" }")
        }
        print("]\n")
    }
}