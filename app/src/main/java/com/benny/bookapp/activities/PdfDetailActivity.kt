package com.benny.bookapp.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.benny.bookapp.Constants
import com.benny.bookapp.MyApplication
import com.benny.bookapp.R
import com.benny.bookapp.adapters.AdapterComment
import com.benny.bookapp.databinding.ActivityPdfDetailBinding
import com.benny.bookapp.databinding.DialogCommentAddBinding
import com.benny.bookapp.models.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream

class PdfDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfDetailBinding

    private companion object {
        const val TAG = "BOOK_DETAILS_TAG"
    }

    //Book id
    private var bookId = ""

    //Get form fb
    private var bookTitle = ""
    private var bookUrl = ""

    private lateinit var firebaseAuth: FirebaseAuth

    private var isInMyFavorite = false

    private lateinit var progressDialog: ProgressDialog

    private lateinit var commentArrayList: ArrayList<ModelComment>

    private lateinit var adapterComment: AdapterComment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!


        //Init progress Dialog
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            checkIsFavorite()
        }


        MyApplication.incrementBookViewCount(bookId)

        loadBookDetails()
        showComments()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId);
            startActivity(intent)
        }

        binding.downloadBookBtn.setOnClickListener {
            //Creating permission
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is already granted")
                downloadBook()

            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION was not granted")
                requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        binding.favoriteBtn.setOnClickListener {

            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, "You're not logged in...", Toast.LENGTH_SHORT).show()
            } else {
                if (isInMyFavorite) {
                    removeFromFavorite()
                } else {
                    addToFavorite()
                }
            }
        }

        binding.addCommentBtn.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, "You're not logged in", Toast.LENGTH_SHORT).show()
            } else {
                addCommentDialog()
            }
        }

    }

    private fun showComments() {

        commentArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentArrayList.clear()
                    for (ds in snapshot.children)
                    {
                        val model = ds.getValue(ModelComment::class.java)

                        commentArrayList.add(model!!)
                    }

                    adapterComment = AdapterComment(this@PdfDetailActivity, commentArrayList)

                    binding.commentsRv.adapter = adapterComment
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private var comment = ""

    private fun addCommentDialog() {
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))

        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setView(commentAddBinding.root)

        val alertDialog = builder.create()
        alertDialog.show()

        commentAddBinding.backBtn.setOnClickListener { alertDialog.dismiss() }

        commentAddBinding.submitBtn.setOnClickListener {
            comment = commentAddBinding.commentEt.text.toString().trim()

            if (comment.isEmpty()){
                Toast.makeText(this, "You're not logged in", Toast.LENGTH_SHORT).show()

            }
            else {
                alertDialog.dismiss()
                addComment()
            }
        }

    }

    private fun addComment() {
        progressDialog.setMessage("Adding Comment")
        progressDialog.show()

        val timeStamp = "${System.currentTimeMillis()}"

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timeStamp"
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timeStamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments").child(timeStamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add comment due to ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is granted")
                downloadBook()
            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is denied")
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }


    private fun downloadBook() {
        //Progress Bar
        Log.d(TAG, "downloadBook: Downloading Book")
        progressDialog.setMessage("Downloading Book")
        progressDialog.show()

        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "downloadBook: Book downloaded...")
                saveToDownLoadFolder(bytes)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.d(TAG, "downloadBook: Failed to delete due to ${e.message}")
                Toast.makeText(this, "Failed to delete due to ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun saveToDownLoadFolder(bytes: ByteArray) {
        Log.d(TAG, "saveToDownLoadFolder: Saving downloaded book")

        val nameWithExtension = "${System.currentTimeMillis()}.pdf"

        try {
            val downloadsFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs()//Creates folder not exists

            val filePath = downloadsFolder.path + "/" + nameWithExtension

            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Toast.makeText(this, "Saved to Downloads Folder...", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "saveToDownLoadFolder: Saved to Downloads Folder")
            progressDialog.dismiss()
            incrementDownloadCount()

        } catch (e: Exception) {
            progressDialog.dismiss()
            Log.d(TAG, "Failed to download due to ${e.message}")
            Toast.makeText(this, "Failed to download due to ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }

    }

    private fun incrementDownloadCount() {

        Log.d(TAG, "incrementDownloadCount: ")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadsCount = "${snapshot.child("downloadsCount").value}"
                    Log.d(TAG, "onDataChange: Current Downloads Count: $downloadsCount")

                    if (downloadsCount == "" || downloadsCount == "null") {
                        downloadsCount = "0"
                    }

                    //Convert to long and increment 1
                    val newDownloadsCount: Long = downloadsCount.toLong() + 1
                    Log.d(TAG, "onDataChange: New Downloads Count: $newDownloadsCount")

                    //Setup data to update to db
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["downloadsCount"] = newDownloadsCount

                    //Update incremented downloads count to db
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: Downloads count incremented")
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "onDataChange: Failed to download due to ${e.message}")

                        }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }

    private fun loadBookDetails() {

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = " ${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    //Format date
                    //val date = MyApplication.formatTimeStamp(timestamp)

                    MyApplication.loadCategory(categoryId, binding.categoryTv)

                    MyApplication.loadPdfFromUrlSinglePage(
                        "$bookUrl",
                        "$bookTitle",
                        binding.pdfView,
                        binding.progressBar,
                        binding.pagesTv,

                    )

                    MyApplication.loadPdfSize("$bookUrl", "$bookTitle", binding.sizeTv)

                    //Get data
                    binding.titleTv.text = bookTitle
                    binding.descriptionTv.text = description
                    binding.viewTv.text = viewsCount
                    binding.downloadTv.text = downloadsCount
                    // binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun checkIsFavorite() {
        Log.d(TAG, "checkIsFavorite: Checking if book is in fav or not")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if (isInMyFavorite) {

                        Log.d(TAG, "onDataChange: Available in favorites")

                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite_filled_white, 0, 0
                        )
                        binding.favoriteBtn.text = "Remove Favorite"
                    } else {
                        Log.d(TAG, "onDataChange:Not available in favorites")

                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite_white, 0, 0
                        )
                        binding.favoriteBtn.text = "Add Favorite"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun addToFavorite() {
        Log.d(TAG, "addToFavorite: Adding to fav")
        val timestamp = System.currentTimeMillis()

        //Setup data to add to database
        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        //Save to database
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "addToFavorite: Added to fav")
                Toast.makeText(this, "Added to Fav", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { e ->
                Log.d(TAG, "addToFavorite: Failed to add due to ${e.message}")
                Toast.makeText(this, "Failed to add due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    public fun removeFromFavorite() {
        Log.d(TAG, "removeFromFavorite: Remove from fav")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "addToFavorite: Removed from Fav")
                Toast.makeText(this, "Removed from Fav", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { e ->
                Log.d(
                    TAG,
                    "removeFromFavorite: Failed to remove from favorites due to ${e.message}"
                )
                Toast.makeText(
                    this,
                    "Failed to remove form favorites due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


}