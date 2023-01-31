package com.moongchipicker.util

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.math.roundToInt


internal fun <T> Array<T>.toDebugString(): String {
    return "{ ${joinToString(separator = ", ")} }"
}


internal inline fun <T> sdkAndUp(sdkVersion: Int, onSdk: () -> T): T? {
    return if (Build.VERSION.SDK_INT >= sdkVersion) {
        onSdk()
    } else null
}

internal fun <T> List<T>?.toSafe(): List<T> {
    return this ?: emptyList()
}

internal fun Boolean?.toSafe(): Boolean {
    return this ?: false
}

internal fun Int?.toSafe(): Int {
    return this ?: 0
}

internal fun String?.toSafe(): String {
    return this ?: ""
}

internal fun Context.dpToPx(dp: Float): Int {
    return (dp * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}

internal fun <T> MutableLiveData<T>.toLiveData(): LiveData<T> {
    return this
}

inline fun <reified R> Any?.whatIfNotNullAs(
    whatIf: (R) -> Unit
): Any? {
    return whatIfNotNullAs(
        whatIf = whatIf,
        whatIfNot = { }
    )
}

inline fun <reified R> Any?.whatIfNotNullAs(
    whatIf: (R) -> Unit,
    whatIfNot: () -> Unit
): Any? {

    if (this != null && this is R) {
        whatIf(this as R)
        return this
    }
    whatIfNot()
    return this
}

internal fun Fragment.setResult(requestKey : String, result : Bundle) {
    parentFragmentManager.setFragmentResult(
        requestKey,
        result
    )
}

