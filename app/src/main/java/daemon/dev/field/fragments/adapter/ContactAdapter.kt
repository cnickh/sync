package daemon.dev.field.fragments.adapter

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.marginEnd
import androidx.core.view.setPadding
import daemon.dev.field.R


class ContactAdapter(val context: Context, val contacts: Array<String>) : BaseAdapter(), Filterable, ListAdapter{

    private lateinit var filteredData : ArrayList<String>

    override fun getFilter(): Filter {
        return ItemFilter()
    }

    override fun getCount(): Int {
        return filteredData.size
    }

    override fun getItem(p0: Int): String? {

        return if(p0 < filteredData.size){
            filteredData[p0]
        }else {
            null
        }

    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

        val view = TextView(context)
        view.setPadding(2)
        view.textSize = 18F
        view.setTextColor(Color.BLACK)

        if(p0 < filteredData.size){
            view.text = filteredData[p0]
        }

        return view

    }

    inner class ItemFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {

            val originalData : MutableList<String> = mutableListOf()

            for (i in contacts){
                originalData.add(i)
            }

            val filterString = constraint.toString().toLowerCase()
            val results = FilterResults()
            val list: List<String> = originalData
            val count = list.size
            val nlist = ArrayList<String>(count)
            var filterableString: String
            for (i in 0 until count) {
                filterableString = list[i]
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(filterableString)
                }
            }
            results.values = nlist
            results.count = nlist.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredData = results?.values as ArrayList<String>
            notifyDataSetChanged()
        }
    }
}
