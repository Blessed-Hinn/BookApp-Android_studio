package com.benny.bookapp

import android.widget.Filter

class FilterCategory : Filter {


    //arrayList in which we want to search
    private val filterList: ArrayList<ModelCategory>

    //adapter in which filter need to be implemented
    private val adapterCategory: AdapterCategory

    //Constructor
    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        //Value should not null and not empty
        if (constraint != null && constraint.isNotEmpty()) {
            //Searched value is neither null nor empty

            //Change to uppercase, or lowercase to avoid case sensitive
            constraint = constraint.toString().uppercase()
            val filteredModels: ArrayList<ModelCategory> = ArrayList()
            for (i in 0 until filterList.size) {
                //Validate
                if (filterList[i].category.uppercase().contains(constraint)) {
                    //add to filtered list
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels


        } else {
            //Search value is either null or empty
            results.count = filterList.size
            results.values = filterList
        }

        return results //Don't miss it
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //Apply filter
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>

        //Notify changes
        adapterCategory.notifyDataSetChanged()

    }
}