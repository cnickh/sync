package daemon.dev.field.fragments.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import daemon.dev.field.R
import daemon.dev.field.data.PostRAM
import daemon.dev.field.databinding.BinViewHolderBinding

@RequiresApi(Build.VERSION_CODES.O)
class ChannelAdapter(val view : View)  : RecyclerView.Adapter<ChannelAdapter.BinVh>(){

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


    inner class BinVh(private val binding: BinViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bin: String?) {

            bin?.let { bin_id ->

                binding.binName.text = bin_id

                if(PostRAM.binSel.contains(bin_id)){
                    binding.bin.setBackgroundResource(R.drawable.col_bg)
                }else{
                    binding.bin.setBackgroundResource(R.drawable.wht_bg)
                }

                binding.bin.setOnClickListener {
                    if(PostRAM.selectChannel(bin_id)){
                        binding.bin.setBackgroundResource(R.drawable.col_bg)
                    }else{
                        binding.bin.setBackgroundResource(R.drawable.wht_bg)
                    }
                }

            }
        }
    }

}