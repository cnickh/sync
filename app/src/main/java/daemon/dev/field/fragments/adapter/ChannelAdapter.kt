package daemon.dev.field.fragments.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import daemon.dev.field.R
import daemon.dev.field.cereal.objects.Channel
import daemon.dev.field.databinding.BinViewHolderBinding
import daemon.dev.field.fragments.dialogs.JoinDialog
import daemon.dev.field.fragments.model.SyncModel
import daemon.dev.field.network.Sync

@RequiresApi(Build.VERSION_CODES.O)
class ChannelAdapter(val activity : FragmentActivity, val view : View, val vm : SyncModel)  : RecyclerView.Adapter<ChannelAdapter.BinVh>(){

    private var itemsList: MutableList<Channel> = arrayListOf()

    override fun getItemCount() = itemsList.size

    private fun getItem(position: Int): Channel? = if (itemsList.isEmpty()) null else itemsList[position]

    fun updateView(list:List<Channel>) {
        if(list != itemsList){
            itemsList = list as MutableList<Channel>
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

        fun bind(bin: Channel?) {

            bin?.let {

                Log.d("ChannelAdapter,kt","rebinding ${it.name} with ${it.key}")

                if(it.key != "null"){
                    Log.d("ChannelAdapter,kt","setUpChannel(binding, it)")
                    setUpChannel(binding, it)
                }else{
                    Log.d("ChannelAdapter,kt","setUpJoin(binding, it)")
                    setUpJoin(binding, it)
                }

            }
        }

        private fun setUpChannel(binding: BinViewHolderBinding, bin : Channel){
            val bin_id = bin.name

            binding.binName.text = bin_id
            binding.badge.visibility = View.INVISIBLE

            if(Sync.open_channels.contains(bin_id)){
                binding.bin.setBackgroundResource(R.drawable.col_bg)
            }else{
                binding.bin.setBackgroundResource(R.drawable.wht_bg)
            }

            binding.bin.setOnClickListener {
                if(vm.selectChannel(bin_id)){
                    binding.bin.setBackgroundResource(R.drawable.col_bg)
                }else{
                    binding.bin.setBackgroundResource(R.drawable.wht_bg)
                }
            }
        }

        private fun setUpJoin(binding: BinViewHolderBinding, bin : Channel) {
            val bin_id = bin.name

            binding.binName.text = bin_id

            binding.badge.visibility = View.VISIBLE

            binding.bin.setOnClickListener {
                createBin(bin.name)
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createBin(name : String) {
        activity.let {

            val dialog = JoinDialog(it,vm,name)

            dialog.window?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT)
            );

            dialog.window?.clearFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            dialog.show()

        }
    }

}