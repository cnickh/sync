package daemon.dev.field.fragments.adapter

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import daemon.dev.field.R
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.databinding.DeviceViewHolderBinding
import daemon.dev.field.fragments.ProfileSelectFragment
import daemon.dev.field.fragments.model.MessengerModel
import daemon.dev.field.util.Phi
import kotlin.math.roundToInt


class DeviceAdapter(val view : View, val activity : FragmentActivity, val msgModel : MessengerModel) : RecyclerView.Adapter<DeviceAdapter.deviceVh>() {

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
        beautify(binding)
        return deviceVh(binding)
    }

    override fun onBindViewHolder(holder: deviceVh, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewDetachedFromWindow(holder: deviceVh) {
        //holder.detach()
    }

//    override fun onViewRecycled(holder: deviceVh) {
//        super.onViewRecycled(holder)
//    }

    inner class deviceVh(private val binding: DeviceViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: User?) {
            Log.d("DEVICEADAPTER.kt","Bind call ${item?.key}")

            item?.let {

                binding.name.text = it.alias
                binding.id.text = it.key.slice(0..9)

                msgModel.getUnRead(it.key)?.let{ unRead ->

                    if(unRead > 0){
                        binding.num.text = unRead.toString()
                        binding.badge.visibility = View.VISIBLE
                    }
                }

                val key = it.key

                if (!msgModel.load(key)){
                    binding.ping.visibility = View.VISIBLE
                    binding.rateText.visibility = View.INVISIBLE
                }

                binding.ping.setOnClickListener {
                    binding.ping.visibility = View.INVISIBLE
                    binding.rateText.visibility = View.VISIBLE
                    msgModel.init_loadBox(key)
                }

                binding.card.setOnClickListener {

                    val bundle = bundleOf("key" to key)

                    val selFrag = ProfileSelectFragment()

                    selFrag.arguments = bundle

                    val ft : FragmentTransaction =
                        activity.supportFragmentManager.beginTransaction();

                    ft.replace(R.id.fragment_view, selFrag);
                    ft.addToBackStack(null)
                    ft.commit();


                }

            }
        }
    }
    private fun beautify(binding: DeviceViewHolderBinding){

        val displayMetrics = view.context.resources.displayMetrics

        val v = binding.card

        val params = v.layoutParams
        val width = displayMetrics.widthPixels - (30*displayMetrics.density)
        params.width = width.roundToInt()
        params.height = Phi().phi(width.roundToInt(),4)
        binding.card.layoutParams = params


        val offset = (params.height - 60*displayMetrics.density)/2

        //set constraints to comment writer
        val set = ConstraintSet()
        set.clone(v)

        set.connect(
            binding.name.id,
            ConstraintSet.START,
            v.id,
            ConstraintSet.START,
            params.height
        )
        set.connect(
            binding.id.id,
            ConstraintSet.START,
            v.id,
            ConstraintSet.START,
            params.height
        )
        set.connect(
            binding.profileImage.id,
            ConstraintSet.START,
            v.id,
            ConstraintSet.START,
            offset.roundToInt()
        )
        set.applyTo(v)

    }

}
