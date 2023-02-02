package com.moongchipicker.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

internal fun Context.isAllPermissionGranted(permissions: List<String>): Boolean {
    return permissions.none { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
}

internal fun Context.checkPermissionGranted(permissions: List<String>): Map<String, Boolean> {
    return permissions.map { it to (ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED) }
        .toMap()
}


val PERMISSION_MEDIA_ACCESS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
} else {
    arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
}


fun ComponentActivity.registerPermissionRequestLauncher(
    onPermissionGranted: () -> Unit,
    withDeniedPermissions: (Array<String>) -> Unit = { Logger.d("permission denied : ${it.toDebugString()}") }
): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionResult ->
        permissionResult.filter { it.value == false }.map { it.key }.toTypedArray()
            .also { deniedPermissions: Array<String> ->
                if (deniedPermissions.isEmpty()) {
                    onPermissionGranted()
                } else {
                    withDeniedPermissions(deniedPermissions)
                }
            }
    }
}

