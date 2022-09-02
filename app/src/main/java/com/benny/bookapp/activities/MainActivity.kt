package com.benny.bookapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.benny.bookapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    //View Binding
    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Handle click, login
        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }


        //Handle click, skip and continue to main screen
        binding.skipBtn.setOnClickListener {
            startActivity(Intent(this, DashboardUserActivity::class.java))
        }


    }
}