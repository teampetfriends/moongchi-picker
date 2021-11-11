package com.moongchipicker.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

internal fun Context.isAllPermissionGranted(permissions: List<String>): Boolean {
    return permissions.none { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
}

internal fun Context.checkPermissionGranted(permissions: List<String>): Map<String, Boolean> {
    return permissions.map { it to (ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED) }
        .toMap()
}


internal fun ComponentActivity.registerPermissionRequestLauncher(
    permissionsToRequest: Array<String>,
    onPermissionGranted: () -> Unit,
    withDeniedPermissions: (Array<String>) -> Unit = { Logger.d("permission denied : ${it.toDebugString()}") }
): PermissionResultLauncher {
    return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionResult ->
        permissionResult.filter { it.value == false }.map { it.key }.toTypedArray()
            .also { deniedPermissions: Array<String> ->
                if (deniedPermissions.isEmpty()) {
                    onPermissionGranted()
                } else {
                    withDeniedPermissions(deniedPermissions)
                }
            }
    }.toPermissionResultLauncher(permissionsToRequest, onPermissionGranted)
}

