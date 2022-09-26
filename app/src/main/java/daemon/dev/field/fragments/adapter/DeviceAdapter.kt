package daemon.dev.field.fragments.adapter

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.databinding.DeviceViewHolderBinding
import daemon.dev.field.databinding.UserActionsBinding
import daemon.dev.field.fragments.model.SyncModel
import daemon.dev.field.util.Expander


@RequiresApi(Build.VERSION_CODES.O)
class DeviceAdapter(val view : View, val vm : SyncModel) : RecyclerView.Adapter<DeviceAdapter.deviceVh>() {

    private var itemsList: List<User> = arrayListOf()

    fun updateView(mutableList: List<User>){

        if(mutableList != itemsList){
            itemsList = mutableList
        }
        notifyDataSetChanged()

    }

    override fun getItemCount() = itemsList.size

    private fun getItem(position: Int):User? = if (itemsList.isEmpty()) null else itemsList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): deviceVh {
        val binding =
            DeviceViewHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return deviceVh(binding)
    }

    override fun onBindViewHolder(holder: deviceVh, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewDetachedFromWindow(holder: deviceVh) {
        holder.detach()
    }

//    override fun onViewRecycled(holder: deviceVh) {
//        super.onViewRecycled(holder)
//    }

    inner class deviceVh(private val binding: DeviceViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var open = false
        val ex = Expander()

        val card = UserActionsBinding.inflate(
            LayoutInflater.from(view.context),
            null,
            false
        )



        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: User?) {
            item?.let {
                binding.name.text = it.alias
                val dev = it.key
                val user = it
                binding.card.setOnClickListener {

                    if(!open) {
                        val targetHeight = it.height + 160
                        ex.expand(it,targetHeight*3,targetHeight)
                        binding.card.addView(card.root)
                        open = true
                    } else {
                        val targetHeight = it.height - 160
                        ex.collapse(it,targetHeight*3,targetHeight)
                        binding.card.removeView(card.root)
                        open = false
                    }

                }

                card.ping.setOnClickListener {
                    val raw = MeshRaw(MeshRaw.PING,null,null,null,null,null)
                    vm.sendToTarget(raw,dev)
                }

                card.disconnect.setOnClickListener {
                    vm.disconnect(user)
                }

            }
        }

        fun detach(){
            binding.card.removeView(card.root)
        }

    }

}
