package com.benny.bookapp

import android.app.Application
import android.app.ProgressDialog
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object{

        /*fun formatTimeStamp(timestamp: Long) : String{
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp

            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }*/
        //Function to get pdf size
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"

            //Getting file and its data from firebase
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetadata ->
                    Log.d(TAG, "loadPdfSize: got metadata")
                    val bytes = storageMetadata.sizeBytes.toDouble()
                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    //Convert bytes to KB/MB
                    val kb = bytes / 1024
                    val mb = kb / 1024
                    if (mb >= 1) {
                        sizeTv.text = "${String.format("%.2f", mb)} MB"
                    } else if (kb >= 1) {
                        sizeTv.text = "${String.format("%.2f", kb)} KB"
                    } else {
                        sizeTv.text = "${String.format("%.2f", bytes)} bytes"
                    }

                }
                .addOnFailureListener { e ->
                    //Failed to get metadata
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")

                }
        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            //pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?,
        ) {

            val TAG = "PDF_THUMBNAIL_TAG"

            //Getting file and its metadata from fb storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->
                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    //SET to PdfView
                    /*pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onPageError {page, t->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onLoad { nbpages ->
                        Log.d(TAG, "LoadPdfFromUrlSinglePage: Pages: $nbPAges")
                            progressBar.visibility = View.INVISIBLE

                            if (pagesTv != null){
                                pagesTv.text = "$nbpages"
                        }*/


                }


                .addOnFailureListener { e ->
                    //Failed to get metadata
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")

                }
        }

        fun loadCategory(categoryId: String, categoryTv: TextView){
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //Get Category
                        val category: String = "${snapshot.child("category").value}"

                        //Set Category
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
        
        fun deleteBook(context: android.content.Context, bookId: String, bookurl: String, bookTitle: String){
            val TAG = "DELETE_BOOK_TAG"

            Log.d(TAG, "deleteBook: deleting...")

            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting $bookTitle...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: Deleting from storage...")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookurl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: deleteBook: Deleted from storage")
                    Log.d(TAG, "deleteBook: Deleting from db now...")

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Successfully deleted...", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "deleteBook: Deleted from db")
                        }
                        .addOnFailureListener { e->
                            Log.d(TAG, "deleteBook: deleteBook: Failed to delete from db due to ${e.message}")
                            Toast.makeText(context, "Failed to db due to ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                }
                .addOnFailureListener {e ->
                    Log.d(TAG, "deleteBook: deleteBook: Failed to delete from storage due to ${e.message}")
                    Toast.makeText(context, "Failed to delete due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        fun incrementBookViewCount(bookId: String){
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {

                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if (viewsCount==""|| viewsCount=="null"){
                            viewsCount = "0";
                        }

                        val newViewsCount = viewsCount.toLong()+1

                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewsCount

                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

        /*public fun removeFromFavorite(context: Context, bookId: String){

            val TAG = "REMOVE_FAV_TAB"

            Log.d(TAG, "removeFromFavorite: Remove from fav")

            val firebaseAuth = FirebaseAuth.getInstance();

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "addToFavorite: Removed from Fav")
                     Toast.makeText(context, "Removed from favorite", Toast.LENGTH_SHORT).

                }
                .addOnFailureListener { e->
                    Log.d(TAG, "removeFromFavorite: Failed to remove from favorites due to ${e.message}")
                    Toast.makeText(context, "Failed to remove form favorites due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }*/
    }

}


