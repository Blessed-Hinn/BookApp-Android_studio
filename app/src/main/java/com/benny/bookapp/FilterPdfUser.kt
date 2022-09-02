package com.benny.bookapp

import android.widget.Filter

class FilterPdfUser : Filter {

    var filterList: ArrayList<ModelPdf>

    var adapterPdfUser: AdapterPdfUser


    //Constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfUser: AdapterPdfUser) : super() {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(contstraint: CharSequence): FilterResults {

        var constraint: CharSequence? = contstraint

        val results = FilterResults()

        if (constraint != null && constraint.isNotEmpty()) {

            constraint = constraint.toString().uppercase()
            val filterModels = ArrayList<ModelPdf>()
            for (i in filterList.indices) {
                if (filterList[i].title.uppercase().contains(constraint)) {
                    filterModels.add(filterList[i])
                }
            }

            results.count = filterModels.size
            results.values = filterModels
        } else {
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(p0: CharSequence?, p1: FilterResults) {
        TODO("Not yet implemented")
    }
}