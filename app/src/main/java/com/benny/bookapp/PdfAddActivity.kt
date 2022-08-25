package com.benny.bookapp

import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.benny.bookapp.databinding.ActivityPdfBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfAddActivity : AppCompatActivity() {

    //Setup view binding
    private lateinit var binding : ActivityPdfBinding

    //Firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth

    //Progress dialog
    private lateinit var progressDialog: ProgressDialog

    //ArrayList to hold pdf
    private lateinit var categoryArrayList : ArrayList<ModelCategory>

    //url of picked pdf
    private var pdfUrl : Uri? = null

    //TAG
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()

        //setup progress Dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait...")
        progressDialog.setCanceledOnTouchOutside(false)
    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf Categories")

        //init array List
        categoryArrayList = ArrayList()

        //db reference to load categories DB > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent( object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}