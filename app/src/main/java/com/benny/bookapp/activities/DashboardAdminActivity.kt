package com.benny.bookapp.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.benny.bookapp.adapters.AdapterCategory
import com.benny.bookapp.databinding.ActivityDashboardAdminBinding
import com.benny.bookapp.models.ModelCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardAdminActivity : AppCompatActivity() {

    //View binding
    private lateinit var binding: ActivityDashboardAdminBinding

    //firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth


    //ArrayList to hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //Adapter
    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        //Search
        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //Called as and when user type anything
                try {
                    adapterCategory.filter.filter(s)
                }
                catch (e: Exception){

                }
            }

            override fun afterTextChanged(p0: Editable?) {
                TODO("Not yet implemented")
            }
        })


        //handle click, log out
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        //Handle Click, Add Category
        binding.addCategory.setOnClickListener {
            startActivity(Intent(this, CategoryAddActivity::class.java))
        }

        //Handle Click, Add Pdf page
        binding.addPdfFab.setOnClickListener {
            startActivity(Intent(this, PdfAddActivity::class.java))
        }

        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun loadCategories() {
        //init arrayList
        categoryArrayList = ArrayList()

        //get all categories from firebase db, Firebase DB > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //Clear list before adding data into it
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    //get data as model
                    val model = ds.getValue(ModelCategory::class.java)

                    //Add to arrayList
                    categoryArrayList.add(model!!)

                }

                //Setup adapter
                adapterCategory = AdapterCategory(this@DashboardAdminActivity, categoryArrayList)

                //set adapter to recyclerview
                binding.categoriesRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private fun checkUser() {
        //get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //not logged in, go to main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            //Logged in, get and show user info
            val email = firebaseUser.email
            //Set to textview of toolbar
            binding.subTitleTv.text = email
        }
    }
}