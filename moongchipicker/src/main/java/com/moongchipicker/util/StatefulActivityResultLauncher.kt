package com.moongchipicker.util

import androidx.activity.result.ActivityResultLauncher


internal open class StatefulActivityResultLauncher<T>(
    private val activityResultLauncher: ActivityResultLauncher<T>,
    private val launchParam: T? = null
) {

    fun launch() {
        if (launchParam != null) {
            activityResultLauncher.launch(launchParam)
        } else {
            Logger.d("launchParam이 없습니다")
        }
    }
}


internal class PermissionResultLauncher(
    activityResultLauncher: ActivityResultLauncher<Array<String>>,
    val permissionsToRequest: Array<String>,
    val onPermissionGranted: () -> Unit
) : StatefulActivityResultLauncher<Array<String>>(activityResultLauncher, permissionsToRequest)


internal fun <T> ActivityResultLauncher<T>.toStatefulActivityResultLauncher(launchParam: T? = null) =
    StatefulActivityResultLauncher(this, launchParam)

internal fun ActivityResultLauncher<Array<String>>.toPermissionResultLauncher(
    launchParam: Array<String>,
    onPermissionGranted: () -> Unit
) = PermissionResultLauncher(this, launchParam, onPermissionGranted)