package daemon.dev.field.fragments.adapter

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import daemon.dev.field.PROFILE_TAG
import daemon.dev.field.R
import daemon.dev.field.databinding.DeviceViewHolderBinding
import daemon.dev.field.fragments.ProfileSelectFragment
import daemon.dev.field.network.PeerRAM
import daemon.dev.field.nypt.Key

@RequiresApi(Build.VERSION_CODES.O)
class DeviceAdapter(val view : View, val activity : FragmentActivity) : RecyclerView.Adapter<DeviceAdapter.deviceVh>() {

    private var itemsList: List<Key> = arrayListOf()

    fun updateView(mutableList: List<Key>){

        if(mutableList != itemsList){
            itemsList = mutableList
        }
        notifyDataSetChanged()

    }

    override fun getItemCount() = itemsList.size

    private fun getItem(position: Int):Key? = if (itemsList.isEmpty()) null else itemsList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): deviceVh {
        val binding =
            DeviceViewHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return deviceVh(binding)
    }

    override fun onBindViewHolder(holder: deviceVh, position: Int) {
        holder.bind(getItem(position))
    }

    inner class deviceVh(private val binding: DeviceViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: Key?) {
            item?.let {

                val info = PeerRAM.getNodeInfo(it)

                if(info == null){
                    Log.i(PROFILE_TAG, "No User info avail for ${item.toHex()}")
                }

                info?.let{ info ->
                    binding.name.text = info.user.alias
                }

                binding.card.setOnClickListener {
                   // val bundle = bundleOf("uid" to info!!.user.id)

                    val selFrag = ProfileSelectFragment()

                    //selFrag.arguments = bundle

                    val ft : FragmentTransaction =
                        activity.supportFragmentManager.beginTransaction();

                    ft.replace(R.id.fragment_view, selFrag, "FRAGMENT_TAG");
                    ft.commit();

                }

            }
        }

    }

}
