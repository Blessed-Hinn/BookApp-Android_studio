package com.benny.bookapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.benny.bookapp.BookUserFragment
import com.benny.bookapp.R
import com.benny.bookapp.databinding.ActivityDashboardUserBinding
import com.benny.bookapp.models.ModelCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DashboardUserActivity : AppCompatActivity() {

    //View binding
    private lateinit var binding: ActivityDashboardUserBinding

    //Firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private lateinit var viewPageAdapter: ViewpagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_dashboard_user)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager){
        viewPageAdapter = ViewpagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this
        )

        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object  : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()

                val modelAll = ModelCategory("01", "All", 1, "")
                val modelMostViewed = ModelCategory("01", "Most Viewed", 1, "")
                val modelMostDownloaded = ModelCategory("01", "Most Downloaded", 1, "")

                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelMostDownloaded)
                viewPageAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}"
                    ), modelAll.category
                )
                viewPageAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}"
                    ), modelMostViewed.category
                )
                viewPageAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modelMostDownloaded.id}",
                        "${modelMostDownloaded.category}",
                        "${modelMostDownloaded.uid}"
                    ), modelMostDownloaded.category
                )
                viewPageAdapter.notifyDataSetChanged()

                for (ds in snapshot.children){

                    val model = ds.getValue(ModelCategory::class.java)

                    categoryArrayList.add(model!!)

                    viewPageAdapter.addFragment(
                        BookUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ), model.category
                    )

                    viewPageAdapter.notifyDataSetChanged()

                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        viewPager.adapter = viewPageAdapter
    }


    class ViewpagerAdapter(fm: FragmentManager, behavior: Int, context: Context): FragmentPagerAdapter(fm ,behavior){

        private val fragmentList: ArrayList<BookUserFragment> = ArrayList()

        private val fragmentTitleList: ArrayList<String> = ArrayList()

        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: BookUserFragment, title: String){
            fragmentList.add(fragment)

            fragmentTitleList.add(title)
        }
    }

    private fun checkUser() {
        //Get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //not logged in, user can stay in user dashboard without loggin in
            binding.subTitleTv.text = "Not Logged in"

            binding.logoutBtn.visibility = View.GONE
            binding.profileBtn.visibility = View.GONE
        }
        else {
            //Logged in, get and show user info
            val email =  firebaseUser.email
            //Set to textview of toolbar
            binding.subTitleTv.text = email

            binding.logoutBtn.visibility = View.VISIBLE
            binding.profileBtn.visibility = View.VISIBLE
        }
    }
}