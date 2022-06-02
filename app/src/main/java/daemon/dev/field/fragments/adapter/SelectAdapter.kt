package daemon.dev.field.fragments.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import daemon.dev.field.R
import daemon.dev.field.databinding.BinViewHolderBinding

class SelectAdapter  : RecyclerView.Adapter<SelectAdapter.BinVh>(){

    private var selectedList : MutableList<String> = mutableListOf()
    private var itemsList: MutableList<String> = arrayListOf()

    override fun getItemCount() = itemsList.size

    private fun getItem(position: Int): String? = if (itemsList.isEmpty()) null else itemsList[position]

    fun updateView(list:List<String>) {
        if(list != itemsList){
            itemsList = list as MutableList<String>
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BinVh {
        val binding =
            BinViewHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BinVh(binding)
    }

    override fun onBindViewHolder(holder: BinVh, position: Int) {
        holder.bind(getItem(position))
    }

    fun getSelected() : List<String> {
        return selectedList
    }

    inner class BinVh(private val binding: BinViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bin: String?) {

            bin?.let { bin_id ->

                binding.binName.text = bin_id
                binding.bin.setOnClickListener {

                    if(!selectedList.contains(bin_id)){
                        selectedList.add(bin_id)
                        binding.bin.setBackgroundResource(R.drawable.col_bg)
                    } else {
                        selectedList.remove(bin_id)
                        binding.bin.setBackgroundResource(R.drawable.wht_bg)
                    }

                }

            }
        }
    }

}