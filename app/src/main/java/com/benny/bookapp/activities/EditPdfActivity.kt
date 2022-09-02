package com.benny.bookapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.benny.bookapp.databinding.ActivityEditPdfBinding

class EditPdfActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPdfBinding.inflate(layoutInflater)

    }
}