package com.benny.bookapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.benny.bookapp.databinding.ActivityLoginBinding
import com.benny.bookapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    //View binding
    private lateinit var binding:ActivityLoginBinding

    //Firebase auth
    //TODO launch the authorization
    private lateinit var firebaseAuth: FirebaseAuth

    //Progress Dialog
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dialog, will show while logging in user
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click, not have account
        binding.noAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        //handle click, login
        binding.loginBtn.setOnClickListener {
            /*
            * 1) Input data
            * 2) validate Data
            * 3) Login - Firebase Auth
            * 4) Check User
             */
            validateData()
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        //1) Input data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        //2) Validate Data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid Email...", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()){
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show()
        }
        else {
            loginUser()
        }
    }

    private fun loginUser() {
        // 3) Login - Firebase Auth

    }
}