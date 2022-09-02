package com.benny.bookapp.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.benny.bookapp.R
import com.benny.bookapp.databinding.ActivityProfileEditBinding
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private var imageUrl: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.profileTv.setOnClickListener {
            showImageAttachment()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
        
    private fun validateData() {

        name = binding.nameEt.text.toString().trim()
        
        if (name.isEmpty()){
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show()
        }
        else {
            
            if (imageUrl == null){
                updateProfile("")
            }
            else {
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        progressDialog.setMessage("Uploading profile image")
        progressDialog.show()

        val filePathAndName = "ProfileImages/"+firebaseAuth.uid

        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUrl!!)
            .addOnSuccessListener { taskSnapshot->

                val urlTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!urlTask.isSuccessful);
                    val uploadedImageUrl = "${urlTask.result}"
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload Image due to ${e.message}", Toast.LENGTH_SHORT).show()

            }

    }

    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setMessage("Updating progfile...")

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["name"] = "$name"
        if (imageUrl != null){
            hashMap["profileImage"] = uploadedImageUrl

        }

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to update profile due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserInfo() {

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"

                    //val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())

                    binding.nameEt.setText(name)

                    try {
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileTv)
                    } catch (e: Exception) {

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun showImageAttachment() {

        val popupMenu = PopupMenu(this, binding.profileTv)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Camera")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->

            val id = item.itemId
            if (id == 0) {
                pickImageCamera()
            } else if (id == 1) {
                pickImageGallery()
            }

            true

        }
    }

    private fun pickImageCamera() {

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

        imageUrl = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUrl)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery() {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "images/*"
            galleryActivityResultLauncher.launch(intent)
    }


    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUrl = data!!.data

                binding.profileTv.setImageURI(imageUrl)
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUrl = data!!.data

                binding.profileTv.setImageURI(imageUrl)
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )
}
