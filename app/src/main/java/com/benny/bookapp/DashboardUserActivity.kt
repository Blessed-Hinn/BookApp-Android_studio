package com.benny.bookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.benny.bookapp.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth


class DashboardUserActivity : AppCompatActivity() {

    //View binding
    private lateinit var binding: ActivityDashboardUserBinding

    //Firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_dashboard_user)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }

    private fun checkUser() {
        //Get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //not logged in, user can stay in user dashboard without loggin in
            binding.subTitleTv.text = "Not Logged in"
        }
        else {
            //Logged in, get and show user info
            val email =  firebaseUser.email
            //Set to textview of toolbar
            binding.subTitleTv.text = email
        }
    }
}