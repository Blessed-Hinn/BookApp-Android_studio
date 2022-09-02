package com.benny.bookapp.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.benny.bookapp.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class CategoryAddActivity : AppCompatActivity() {

    //View Binding
    private lateinit var binding: ActivityCategoryAddBinding

    //declare firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //Declare progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //Configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        //Handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //Handle click, begin upload category
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var category = ""

    private fun validateData() {
        //Validate data

        //Get data
        category = binding.categoryEt.text.toString().trim()

        //Validate data
        if (category.isEmpty()) {
            Toast.makeText(this, "Enter Category...", Toast.LENGTH_SHORT).show()
        } else {
            addCategoryFirebase()
        }

    }

    private fun addCategoryFirebase() {
        //Show progress
        progressDialog.show()

        //Get timestamp
        val timestamp = System.currentTimeMillis()

        //Setup data to add in Firebase db
        val hashMap =
            HashMap<String, Any>()//Second param is any, because the value could be of any type
        hashMap["id"] =
            "$timestamp"//Put in string quotes because timestamp is in double, we need in string for id
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        //Add to firebase db: Database Root > Categories > categoryId > category info
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Added successfully...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                //Failed to add
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}