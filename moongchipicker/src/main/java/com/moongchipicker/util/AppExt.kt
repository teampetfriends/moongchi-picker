package com.moongchipicker.util

import android.os.Build

internal fun <T> Array<T>.toDebugString(): String {
    return "{ ${joinToString(separator = ", ")} }"
}


internal inline fun <T> sdkAndUp(sdkVersion: Int, onSdk: () -> T): T? {
    return if (Build.VERSION.SDK_INT >= sdkVersion) {
        onSdk()
    } else null
}

internal fun <T> List<T>?.toSafe() : List<T> {
    return this ?: emptyList()
}

internal fun Boolean?.toSafe() : Boolean {
    return this ?: false
}

internal fun Int?.toSafe() : Int{
    return this ?: 0
}

internal fun String?.toSafe() : String{
    return this ?: ""
}