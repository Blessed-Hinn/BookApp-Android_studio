package com.benny.bookapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Patterns
import android.widget.Toast
import com.benny.bookapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    //View Binding
    private lateinit var binding: ActivityRegisterBinding

    //Firebase auth
    //TODO launch the authorization
    private lateinit var firebaseAuth: FirebaseAuth

    //Progress Dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dialog, will show while creating account | Register user
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle back button click
        binding.backBtn.setOnClickListener {
            onBackPressed() //Goto previous page
        }

        //Handle click, begin register
        binding.registerButton.setOnClickListener {
            /*
            * 1) Input Data
            * 2) Validate Data
            * 3) Create Account
            * 4) Save user Info
             */
            validateData()
        }

    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {
        //Input Data
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        //2) Validate Data
        if (name.isEmpty()) {
            //Empty name
            Toast.makeText(this, "Enter your name...", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //Invalid Email Address
            Toast.makeText(this, "Invalid Email Address...", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            //Empty Password
            Toast.makeText(this, "Enter Password...", Toast.LENGTH_SHORT).show()
        } else if (cPassword.isEmpty()) {
            //Empty Password
            Toast.makeText(this, "Confirm Password...", Toast.LENGTH_SHORT).show()
        } else if (password != cPassword) {
            //Mismatching passwords
            Toast.makeText(this, "Password doesn't match...", Toast.LENGTH_SHORT).show()
        } else {
            //3)Create User
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        //3) Create Account - Firebase Auth

        //show progress
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()

        //create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //Account created, now add user info to db
                updateUserInfo()

            }
            .addOnFailureListener { e ->
                //Failed creating account
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed creating account due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    private fun updateUserInfo() {
        //4) Save user Info - Firebase Realtime db

        progressDialog.setMessage("Saving user info...")

        //Timestamp
        val timestamp = System.currentTimeMillis()

        //Get current user uid, since user is registered.
        val uid = firebaseAuth.uid

        //Setup data to add in db
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = "" //TODO Add later
        hashMap["userType"] = "user" //possible values are user/admin
        hashMap["timestamp"] = timestamp

        //Set data to db
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
            //User info saved, open user dashboard
                progressDialog.dismiss()
                Toast.makeText(this, "Account created...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                //Failed Adding data
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed saving user info due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }
}