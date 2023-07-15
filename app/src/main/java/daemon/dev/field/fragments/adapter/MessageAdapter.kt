package daemon.dev.field.fragments.adapter

import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.R
import daemon.dev.field.cereal.objects.Comment
import daemon.dev.field.databinding.MessageHolderBinding
import daemon.dev.field.fragments.model.MessengerModel
import java.util.*

class MessageAdapter (val view : View, val activity : FragmentActivity) : RecyclerView.Adapter<MessageAdapter.MessageVh>() {

    private var itemsList: List<Comment> = arrayListOf()

    fun updateView(mutableList: List<Comment>) {

        if (mutableList != itemsList) {
            itemsList = mutableList
        }
        notifyDataSetChanged()

    }

    override fun getItemCount() = itemsList.size

    private fun getItem(position: Int): Comment? =
        if (itemsList.isEmpty()) null else itemsList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageVh {
        val binding =
            MessageHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return MessageVh(binding)
    }

    override fun onBindViewHolder(holder: MessageVh, position: Int) {

        holder.bind(getItem(position))
    }

    override fun onViewDetachedFromWindow(holder: MessageVh) {
        //holder.detach()
    }

//    override fun onViewRecycled(holder: deviceVh) {
//        super.onViewRecycled(holder)
//    }

    inner class MessageVh(private val binding: MessageHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: Comment?) {
            item?.let {

                if(it.key == PUBLIC_KEY.toBase64()){
                    addMyMessage(it.comment,binding)
                }else{
                    addRemoteMessage(it,binding)
                }

            }
        }
        private fun addMyMessage(msg : String, nwCard : MessageHolderBinding){
            nwCard.text.text = msg
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

            params.gravity = GravityCompat.END
            //params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            nwCard.messageCard.layoutParams = params
        }

        private fun addRemoteMessage(msg : Comment, nwCard : MessageHolderBinding){
            nwCard.text.text = msg.comment
            nwCard.messageCard.setBackgroundResource(R.drawable.cmt_left)

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

            params.gravity = GravityCompat.START

            nwCard.messageCard.layoutParams = params
        }

        private fun ByteArray.toBase64() : String {
            return Base64.getEncoder().encodeToString(this)
        }
    }
}