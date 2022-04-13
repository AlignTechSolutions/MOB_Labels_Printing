package com.alignTech.labelsPrinting.ui.oneSingleActivity.screns.main.labelsPrinting.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.alfayedoficial.kotlinutils.kuToast
import com.alignTech.labelsPrinting.R
import com.alignTech.labelsPrinting.local.model.labelsPrinting.LabelsPrinting

class NameProductAutoCompleteAdapter(
    context: Context,
    private val resource: Int,
    objects: ArrayList<LabelsPrinting>
) : ArrayAdapter<LabelsPrinting>(context, resource, objects) {

    private var locations: ArrayList<LabelsPrinting> = objects
    private var filtered: ArrayList<LabelsPrinting> = objects

    init {
        if (filtered.isNullOrEmpty()) {
            context.kuToast("لا يوجد")
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        try {
            val layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = layoutInflater.inflate(resource, parent, false)
            val tvItemName = v!!.findViewById<TextView>(R.id.branch_name)
            tvItemName.text = filtered[position].nameProduct
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return v!!
    }

    override fun getCount(): Int = filtered.size

    override fun getItem(position: Int): LabelsPrinting = filtered[position]

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                try {
                    filtered = if (charSequence.isNotEmpty()) {
                        val charString = charSequence.toString().trim()
                        if (charString.isEmpty()) {
                            locations
                        } else {
                            locations.filter {
                                it.nameProduct!!.trim().contains(charSequence)
                            }.toCollection(ArrayList())
                        }
                    } else locations
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val filterResults = FilterResults()
                filterResults.values = filtered
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                try {
                    filtered = filterResults.values as ArrayList<LabelsPrinting>
                    notifyDataSetChanged()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}