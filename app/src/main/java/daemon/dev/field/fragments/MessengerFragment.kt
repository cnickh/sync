package daemon.dev.field.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import daemon.dev.field.MF_TAG
import daemon.dev.field.R
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.databinding.*
import daemon.dev.field.fragments.adapter.MessageAdapter
import daemon.dev.field.fragments.model.MessengerModel
import daemon.dev.field.fragments.model.ResourceModel
import daemon.dev.field.fragments.model.SyncModel
import daemon.dev.field.network.Async

class MessengerFragment: Fragment() {
    private val messenger: MessengerModel by activityViewModels()
    private val syncModel : SyncModel by activityViewModels()
    private val resModel : ResourceModel by activityViewModels()

    private lateinit var binding: MessengerFragmentBinding

    private lateinit var key: String

    private lateinit var messageAdapter : MessageAdapter


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
        messenger.printMsgMap()
        messenger.createSub(key)
        var sub = messenger.getSub(key)!!

        Log.i(MF_TAG,"Have sub $sub")

        messageAdapter = activity?.let { MessageAdapter(view, it) }!!
        binding.messageList.layoutManager = LinearLayoutManager(context)
        binding.messageList.adapter = messageAdapter

        messageAdapter.updateView(sub)
        binding.scroll.fullScroll(View.FOCUS_DOWN)

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
            binding.messageText.setText("")//text.clear()
            messenger.send(mesg,key)
        }

        binding.close.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commit()
        }

        messenger.latest.observe(viewLifecycleOwner) {

            Log.i(MF_TAG,"Firing on latest :: $it")
            sub = messenger.getSub(key)!!
            messageAdapter.updateView(sub)

           // binding.scroll.fullScroll(View.FOCUS_DOWN)

        }

        binding.messageList.post {
            //binding.scroll.fullScroll(View.FOCUS_DOWN)
        }

        Async.peers.observe(viewLifecycleOwner) { peers ->

            val connected = peers.contains(User(key, "", 0, ""))
            if (connected) {
                binding.connection.setBackgroundResource(R.drawable.circle_col)
            } else {
                binding.connection.setBackgroundResource(R.drawable.circle)
            }

        }

    }

}
