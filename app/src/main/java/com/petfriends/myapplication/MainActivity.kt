package com.petfriends.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.moongchipicker.MoongchiPickerDialog
import com.moongchipicker.PetMediaType
import com.moongchipicker.onMoongchiPickerResult
import com.moongchipicker.showMoongchiPicker
import com.moongchipicker.util.PERMISSION_MEDIA_ACCESS
import com.moongchipicker.util.registerPermissionRequestLauncher
import com.petfriends.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionLauncher = registerPermissionRequestLauncher(
            onPermissionGranted = { showMoongchiPicker(MoongchiPickerDialog.DialogInfo(PetMediaType.IMAGE, 3)) },
            withDeniedPermissions = {}
        )

        binding.iv.setOnClickListener {
            permissionLauncher.launch(PERMISSION_MEDIA_ACCESS)
        }

        onMoongchiPickerResult {
            when (it) {
                is MoongchiPickerDialog.DialogResult.Success -> {
                    val bitmap = BitmapHelper.getBitmapFromUri(it.mediaUriList.first(), contentResolver)
                    binding.iv.setImageBitmap(bitmap)
                }
                is MoongchiPickerDialog.DialogResult.Failure -> {
                    Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}

