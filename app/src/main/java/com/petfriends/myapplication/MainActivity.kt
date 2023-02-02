package com.petfriends.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.moongchipicker.*
import com.moongchipicker.data.MediaType
import com.petfriends.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    private val moongchiPickerLauncher =
        registerForActivityResult(OpenMoongchiPicker()) { result ->
            when (result) {
                is MoongchiPickerResult.Success -> binding.iv.setImageBitmap(
                    BitmapHelper.getBitmapFromUri(result.uriList.first(), contentResolver)
                )
                is MoongchiPickerResult.Failure -> Toast.makeText(
                    this,
                    result.errorMsg,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            iv.setOnClickListener {
                moongchiPickerLauncher.launch(
                    MoongchiPickerDialog.DialogInfo(
                        mediaType = MediaType.IMAGE,
                        maxSelectableMediaCount = 5
                    )
                )
            }
        }

    }
}

