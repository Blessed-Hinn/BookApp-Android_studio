package com.benny.bookapp.filters

import android.widget.Filter
import com.benny.bookapp.adapters.AdapterPdfAdmin
import com.benny.bookapp.models.ModelPdf


class FilterPdfAdmin : Filter {
    var filterList: ArrayList<ModelPdf>

    var adapterPdfAdmin: AdapterPdfAdmin

    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint//value to search
        val results = FilterResults()
        //Value to be searched
        if (constraint != null && constraint.isNotEmpty()){
            constraint = constraint.toString().lowercase()
            var filteredModel = ArrayList<ModelPdf>()
            for (i in filterList.indices){
                //Validate if match
                if (filterList[i].title.lowercase().contains(constraint)){
                    filteredModel.add(filterList[i])
                }
            }
            results.count = filteredModel.size
            results.values = filteredModel
        }
        else{
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        adapterPdfAdmin.pdfArrayList = results!!.values as ArrayList<ModelPdf>

        adapterPdfAdmin.notifyDataSetChanged()
    }


}