package com.benny.bookapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.benny.bookapp.filters.FilterCategory
import com.benny.bookapp.models.ModelCategory
import com.benny.bookapp.activities.PdfListAdminActivity
import com.benny.bookapp.databinding.RowCategoryBinding
import com.google.firebase.database.FirebaseDatabase


class AdapterCategory : RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable {

    private val context: Context
    public var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var binding: RowCategoryBinding
    private var filterList: ArrayList<ModelCategory>

    private var filter: FilterCategory? = null




    //Constructor
    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        //Inflate
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderCategory(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        /*------Get data, Set data, Handle click delete-*/

        //Get data
        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val timestamp = model.timestamp

        //Set data
        holder.deleteBtn.setOnClickListener {
            //Confirm before delete
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton("Confirm") { a, d ->
                    Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show()
                    deleteCategory(model, holder)
                }
                .setNegativeButton("Cancel") { a, d ->
                    a.dismiss()
                }
                .show()
        }

        //Handle click, start pdf list admin activity, also pas pdf id, title
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfListAdminActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("category", category)
            context.startActivity(intent)
        }
    }

    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {
        //Get id of categories
        val id = model.id
        //Firebase BD > Categories > categoryId
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Deleted successfully...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Unable to delete due to ${e.message}", Toast.LENGTH_SHORT)
                    .show()


            }
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size //Number of items in a list
    }

    //ViewHolder class to hold/view for row_category.xml
    inner class HolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //init ui views
        var categoryTv: TextView = binding.categoryTv
        var deleteBtn: ImageButton = binding.deleteBtn
    }

    override fun getFilter(): Filter {
        if (filter == null ){
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }


}