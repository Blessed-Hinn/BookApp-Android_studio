package com.benny.bookapp.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.benny.bookapp.R
import com.benny.bookapp.databinding.ActivityPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditActivity : AppCompatActivity() {

    //View Binding
    private lateinit var binding: ActivityPdfEditBinding

    private var bookId = ""

    private companion object {
        private const val TAG = "PDF_EDIT_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var categoryTitleArraylist: ArrayList<String>

    private lateinit var categoryIdArraylist: ArrayList<String>

    //FindViewByIds
    private val categoryTv: TextView = findViewById(R.id.categoryTv)
    private val submitBtn: Button = findViewById(R.id.submitBtn)
    private val titleEt: EditText = findViewById(R.id.titleEt)
    private val descriptionEt: EditText = findViewById(R.id.descriptionEt)




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        //setContentView(binding.root)


        bookId = intent.getStringExtra("bookId")!!

        //Setup progress Dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadBookInfo()


        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        categoryTv.setOnClickListener {
            categoryDialog()
        }

        submitBtn.setOnClickListener {
            validateData()
        }
    }

    private fun loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book info")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    titleEt.setText(title)
                    descriptionEt.setText(description)

                    Log.d(TAG, "onDataChange: Loadind book category info")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val category = snapshot.child("category").value
                                categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    
                }
            })
    }


    private var title = ""
    private var description = ""

    private fun validateData() {
        title = titleEt.text.toString().trim()
        description = descriptionEt.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show()
        } else if (selectedCategoryId.isEmpty()) {
            Toast.makeText(this, "Pick Category", Toast.LENGTH_SHORT).show()
        } else {
            updatePdf()
        }
    }

    private fun updatePdf() {
        Log.d(TAG, "updatePdf: Starting updating pdf info...")

        //Show Progress
        progressDialog.setMessage("Updating book Info...")
        progressDialog.show()

        //Setup data to update to db
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryyId"] = "$selectedCategoryId"

        //Start Updating
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "updatePdf: Updated successfully...")
                Toast.makeText(this, "Updated successfully...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "updatePdf: Failed to update due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to update due to ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }


    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""


    private fun categoryDialog() {

        val categoriesArray = arrayOfNulls<String>(categoryTitleArraylist.size)
        for (i in categoryTitleArraylist.indices) {
            categoriesArray[i] = categoryTitleArraylist[i]
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Category")
            .setItems(categoriesArray) { dialog, position ->
                selectedCategoryId = categoryIdArraylist[position]
                selectedCategoryTitle = categoryTitleArraylist[position]

                categoryTv.text = selectedCategoryTitle
            }
            .show()

    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: loading categories...")

        categoryTitleArraylist = ArrayList()
        categoryIdArraylist = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ///clear list before adding data
                categoryIdArraylist.clear()
                categoryTitleArraylist.clear()

                for (ds in snapshot.children) {

                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArraylist.add(id)
                    categoryTitleArraylist.add(category)

                    Log.d(TAG, "onDataChange: Category ID $id")
                    Log.d(TAG, "onDataChange: Category  $category")
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}