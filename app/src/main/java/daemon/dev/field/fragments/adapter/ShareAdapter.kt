package daemon.dev.field.fragments.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import daemon.dev.field.R
import daemon.dev.field.databinding.BinViewHolderBinding
import daemon.dev.field.fragments.model.DialogModel


class ShareAdapter(exampleList: ArrayList<String>,val dialogModel: DialogModel) : RecyclerView.Adapter<ShareAdapter.BinVh>() {

    private var mExampleList: ArrayList<String>

    init {
        mExampleList = exampleList
    }

    private fun getItem(position: Int): String? = if (mExampleList.isEmpty()) null else mExampleList[position]

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BinVh {
        val binding =
            BinViewHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BinVh(binding)
    }

    override fun onBindViewHolder(holder: BinVh, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int {
        return mExampleList.size
    }

    fun filterList(filteredList: ArrayList<String>) {
        mExampleList = filteredList
        notifyDataSetChanged()
    }

    inner class BinVh(private val binding: BinViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bin: String?) {

            bin?.let { bin_id ->

                binding.binName.text = bin_id

                binding.bin.setOnClickListener {
                    if(dialogModel.selectChannel(bin_id)){
                        binding.bin.setBackgroundResource(R.drawable.col_bg)
                    }else{
                        binding.bin.setBackgroundResource(R.drawable.wht_bg)
                    }
                }

            }
        }
    }

}