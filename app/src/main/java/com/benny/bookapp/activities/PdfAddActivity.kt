package com.benny.bookapp.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.benny.bookapp.databinding.ActivityPdfBinding
import com.benny.bookapp.models.ModelCategory
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfAddActivity : AppCompatActivity() {

    //Setup view binding
    private lateinit var binding: ActivityPdfBinding

    //Firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth

    //Progress dialog
    private lateinit var progressDialog: ProgressDialog

    //ArrayList to hold pdf
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //url of picked pdf
    private var pdfUrl: Uri? = null

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

        //Handle click, goBack
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //Handle click, show categories pick dialog
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        //Handle click, pick pdf intent
        binding.attachPdfBtn.setOnClickListener {
            //2: Upload pdf to firebase
            pdfPickIntent()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        //1: Validate date
        Log.d(TAG, "validateData: validating data")

        //Get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        //Validate data
        if (title.isEmpty()){
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty()){
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show()
        }
        else if(category.isEmpty()) {
            Toast.makeText(this, "Pick Category...", Toast.LENGTH_SHORT).show()
        }
        else if(pdfUrl == null){
            Toast.makeText(this, "Pick PDF file...", Toast.LENGTH_SHORT).show()
        }
        else {
            //data validated, begin upload
                uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        //STEP 2: Upload PDF toe firebase storage
        Log.d(TAG, "uploadPdfToStorage: Uploading to storage")

        //Show progress dialog
        progressDialog.setMessage("Uploading PDF...")
        progressDialog.show()

        //Timestamp
        val timestamp = System.currentTimeMillis()

        //Path of pdf in firebase storage
        val filePathAndNAme = "Books/$timestamp"
        //Storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndNAme)
        storageReference.putFile(pdfUrl!!)
            .addOnSuccessListener {taskSnapshot ->
                Log.d(TAG, "uploadPdfToStorage: PDF uploaded now getting url...")

                //STEP 3: Get url of uploaded pdf
                val urlTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!urlTask.isSuccessful);
                val uploadedPdfUrl = "${urlTask.result}"

                uploadedPdInfoToDb(uploadedPdfUrl, timestamp)
            }
            .addOnFailureListener {e ->
                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    private fun uploadedPdInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        //STEP4: upload Pdf info to firebase db
        Log.d(TAG, "uploadedPdInfoToDb: uploading to db")
        progressDialog.setMessage("Uploading pdf info...")

        //uid of current user
        val uid = firebaseAuth.uid

        //Setup data to upload
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0

        //db reference DB > Books > (Book Info)
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadedPdInfoToDb: uploaded to db")
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded...", Toast.LENGTH_SHORT).show()
                pdfUrl = null
            }
            .addOnFailureListener { e->
                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT).show()
             }
    }


    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf Categories")

        //init array List
        categoryArrayList = ArrayList()

        //db reference to load categories DB > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //Clear List before adding data
                categoryArrayList.clear()

                for (ds in snapshot.children) {
                    //Get data
                    val model = ds.getValue(ModelCategory::class.java)
                    //Add to arrayList
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Showing pdf category pick dialog")

        //Get string array of categories from arrayList
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoriesArray.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray) { dialog, which ->
                //handle item click
                //Get clicked item
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id
                //Set category to textView
                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog: Selected Category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Selected Category Title: $selectedCategoryTitle")
            }
            .show()
    }
    private fun pdfPickIntent(){
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)

    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{ result ->
            if (result.resultCode == RESULT_OK){
                Log.d(TAG, "PDF Picked: ")
                pdfUrl = result.data!!.data
            }
            else {
                Log.d(TAG, "PDF Pick cancelled: ")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )
}