package daemon.dev.field.fragments

import android.os.Bundle
import android.os.SystemClock
import android.os.SystemClock.elapsedRealtime
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.R
import daemon.dev.field.cereal.objects.Comment
import daemon.dev.field.databinding.*
import daemon.dev.field.fragments.model.MessengerModel
import daemon.dev.field.fragments.model.ResourceModel
import daemon.dev.field.fragments.model.SyncModel
import daemon.dev.field.network.Async
import java.util.Base64

class MessengerFragment: Fragment() {
    private val messenger: MessengerModel by activityViewModels()
    private val syncModel : SyncModel by activityViewModels()
    private val resModel : ResourceModel by activityViewModels()

    private lateinit var binding: MessengerFragmentBinding

    private lateinit var key: String


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        key = requireArguments().getString("key")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Retrieve and inflate the layout for this fragment
        binding = MessengerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //setUpTextBox()

        messenger.createSub(key)
        val sub = messenger.getKey(key)!!

        updateSub(sub)

        syncModel.getUser(key).observe(viewLifecycleOwner) {

            if(it!=null){
                binding.userAlias.text = it.alias
            }

        }

        resModel.getUserProfile(key).observe(viewLifecycleOwner) {
            if(it != null){
                val uri = it.toUri()

                Glide
                    .with(this)
                    .load(uri)
                    .circleCrop()
                    .thumbnail()
                    .placeholder(R.drawable.loading_spinner)
                    .into(binding.userImage);
            }
        }

        binding.send.setOnClickListener {

            val mesg = binding.messageText.text.toString()

            val delta = elapsedRealtime() - Async.peerStart[key]!!

            val msg = Comment(PUBLIC_KEY.toBase64(),mesg,delta)
            messenger.send(msg,key)

            addMyMessage(msg)
        }

        binding.close.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commit()
        }

        messenger.direct.observe(viewLifecycleOwner) {

            val sub = messenger.getKey(key)!!
            updateSub(sub)

        }
    }

    private fun updateSub(root : Comment){

        binding.something.removeAllViews()

        root.let{
            for(c in it.sub){
                if(c.key == PUBLIC_KEY.toBase64()){
                    addMyMessage(c)
                }else{
                    addRemoteMessage(c)
                }
            }
        }
    }

    private fun addMyMessage(msg : Comment){
        binding.something


        val nwCard = MessageHolderBinding.inflate(LayoutInflater.from(context), null, false)
        nwCard.text.text = msg.comment

        binding.something.addView(nwCard.root)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)

        params.gravity = Gravity.END

        nwCard.root.layoutParams = params

        binding.messageText.setText("")
        binding.scroll.fullScroll(View.FOCUS_DOWN)
    }

    private fun addRemoteMessage(msg : Comment){
        val nwCard = MessageHolderBinding.inflate(LayoutInflater.from(context), null, false)
        nwCard.text.text = msg.comment
        nwCard.messageCard.setBackgroundResource(R.drawable.cmt_left)

        binding.something.addView(nwCard.root)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)

        params.gravity = Gravity.START

        nwCard.root.layoutParams = params

        binding.scroll.fullScroll(View.FOCUS_DOWN)
    }

    private fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
    }


}
