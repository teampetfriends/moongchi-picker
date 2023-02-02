package com.petfriends.myapplication

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.moongchipicker.MediaType
import com.moongchipicker.MoongchiPickerActivity
import com.moongchipicker.MoongchiPickerDialog
import com.moongchipicker.MoongchiPickerResult
import com.petfriends.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    private val moongchiPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val pickerResult: MoongchiPickerResult =
                    result.data?.getParcelableExtra(MoongchiPickerActivity.KEY_MOONGCHIPICKER_RESULT)
                            as? MoongchiPickerResult ?: return@registerForActivityResult
                when (pickerResult) {
                    is MoongchiPickerResult.Success -> binding.iv.setImageBitmap(
                        BitmapHelper.getBitmapFromUri(pickerResult.uriList.first(), contentResolver)
                    )
                    is MoongchiPickerResult.Failure -> Toast.makeText(
                        this,
                        pickerResult.errorMsg,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            iv.setOnClickListener {
                moongchiPickerLauncher.launch(
                    MoongchiPickerActivity.createIntent(
                        this@MainActivity,
                        MoongchiPickerDialog.DialogInfo(MediaType.IMAGE)
                    )
                )
            }
        }

    }
}

