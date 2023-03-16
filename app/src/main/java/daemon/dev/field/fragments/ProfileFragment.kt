package daemon.dev.field.fragments

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.flexbox.*
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.R
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.databinding.*
import daemon.dev.field.fragments.adapter.DeviceAdapter
import daemon.dev.field.fragments.model.ResourceModel
import daemon.dev.field.fragments.model.SyncModel
import daemon.dev.field.network.Async
import daemon.dev.field.util.Phi
import daemon.dev.field.util.ServiceLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt


class ProfileFragment : Fragment() {

    private val syncModel : SyncModel by activityViewModels()
    private val resModel : ResourceModel by activityViewModels()

    private lateinit var binding: FragmentProfileBinding
    private lateinit var deviceAdapter : DeviceAdapter

    private var syncButton : SyncButtonBinding? = null
    private var readyButton : ReadyButtonBinding? = null

    private var uiState = "IDLE"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        beautify()

        setState()

        resModel.loadProfileImage()

        resModel.liveProfile.observe(viewLifecycleOwner) { uri ->
            if(uri != null){
                Glide
                    .with(this)
                    .load(uri)
                    .circleCrop()
                    .thumbnail()
                    .placeholder(R.drawable.loading_spinner)
                    .into(binding.pic);
            }
        }

        syncModel.state.observe(viewLifecycleOwner) { _ ->
            setState()
        }

        syncModel.me.observe(viewLifecycleOwner) {
            binding.username.text = it.alias
        }

        binding.stateFrame.setOnClickListener {

           CoroutineScope(Dispatchers.IO).launch {
               ServiceLauncher(view.context).checkStartMesh()
            }

            binding.state.text = Async.state2String()
//            val keys = listOf(
//
//                User("ALKFDJ)@#{P","TonyJboyz",0,"None"),
//                User("ALKFDJ)@#{P","Tim Halaberton",0,"None"),
//                User("ALKFDJ)@#{P","HQ_yamean",0,"None")
//
//            )
//
//
//            Log.d("ProfileFragment.kt", "defuq boi")
//            deviceAdapter.updateView(keys)
        }

        binding.key.text = PUBLIC_KEY.toBase64()
        deviceAdapter = activity?.let { DeviceAdapter(view, it) }!!
        binding.userList.adapter = deviceAdapter

        //Black magic fuckary to center items in recycle view
        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.CENTER
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        binding.userList.layoutManager = layoutManager

        syncModel.peers.observe(viewLifecycleOwner) { keys ->
            deviceAdapter.updateView(keys)
        }

        binding.editButton.setOnClickListener {
            editMode()
            Log.i("Fac-Debug" ,"entering edit mode set DONE")

        }

    }

    private fun setState(){
        val state = Async.state2String()

        when(state){
            "IDLE"->{idle()}
            "READY"->{ready()}
            "INSYNC"->{insync()}
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun editMode(){


        val card = EditProfileBinding.inflate(
            LayoutInflater.from(view?.context),
            null,
            false
        )


        binding.base.removeView(binding.profileHeader)
        binding.base.addView(card.root)


        card.root.layoutParams = binding.profileHeader.layoutParams
        card.doneButton.layoutParams = binding.editButton.layoutParams
        card.userInfo1.layoutParams = binding.userInfo.layoutParams

        val old_width = binding.editButton.width
        val old_height = binding.editButton.height


        card.doneButton.updateLayoutParams {
            width = LayoutParams.WRAP_CONTENT
            height = LayoutParams.WRAP_CONTENT
        }


        card.doneButton.setOnTouchListener(OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->                 // PRESSED
                    card.doneButton.setTextColor(Color.MAGENTA)

                MotionEvent.ACTION_UP ->                 // RELEASED
                    card.doneButton.setTextColor(Color.BLACK)
            }
            false
        })


        card.doneButton.setOnClickListener {

            val alias = card.username.text.toString()

            if(alias != ""){
                syncModel.setAlias(alias)
            }

            binding.base.removeView(card.root)
            binding.base.addView(binding.profileHeader)

            binding.profileHeader.layoutParams = card.root.layoutParams
            binding.editButton.layoutParams = card.doneButton.layoutParams
            binding.userInfo.layoutParams = card.userInfo1.layoutParams


            binding.editButton.updateLayoutParams {
                width = old_width
                height = old_height
            }

        }

        resModel.liveProfile.observe(viewLifecycleOwner) { uri ->
            if(uri != null){
                Glide
                    .with(this)
                    .load(uri)
                    .circleCrop()
                    .thumbnail()
                    .placeholder(R.drawable.loading_spinner)
                    .into(card.pic);
            }
        }

        card.camera.setOnClickListener {
            getContent.launch(arrayOf("image/*"))
        }

    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private val getContent = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        //Handle the returned Uri
        val img = uri
        img?.let {
            resModel.setProfileImage(it)
        }

    }

    private fun idle(){

        if(uiState == "IDLE"){return}
        else{uiState = "IDLE"}

        if(readyButton != null){
            binding.userInfo.removeView(readyButton!!.stateFrame)
            binding.stateFrame.layoutParams = readyButton!!.stateFrame.layoutParams
            binding.userInfo.addView(binding.stateFrame)
        }
    }

    private fun ready(){

        if(uiState == "READY"){return}
        else{uiState = "READY"}

        binding.userInfo.removeView(binding.stateFrame)

        if(readyButton == null){
            readyButton = ReadyButtonBinding.inflate(
                LayoutInflater.from(view!!.context),
                null,
                false
            )
            readyButton!!.stateFrame.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    ServiceLauncher(view!!.context).checkKillMesh()
                }
            }
        }

        readyButton!!.stateFrame.layoutParams = binding.stateFrame.layoutParams
        binding.userInfo.addView(readyButton!!.stateFrame)


    }

    private fun insync(){

        if(uiState == "INSYNC"){return}
        else{uiState = "INSYNC"}

        binding.userInfo.removeView(readyButton!!.stateFrame)

        if(syncButton == null){
            syncButton = SyncButtonBinding.inflate(
                LayoutInflater.from(view!!.context),
                null,
                false
            )
            syncButton!!.stateFrame.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    ServiceLauncher(view!!.context).checkKillMesh()
                }
            }
        }
        syncButton!!.stateFrame.layoutParams = readyButton!!.stateFrame.layoutParams
        binding.userInfo.addView(syncButton!!.stateFrame)
    }

    private fun beautify(){

        val displayMetrics = requireContext().resources.displayMetrics
        val params = binding.profileHeader.layoutParams
        val width = displayMetrics.widthPixels - (30*displayMetrics.density)
        params.width = width.roundToInt()
        params.height = Phi().phi(width.roundToInt(),1)

        binding.profileHeader.layoutParams = params
        val set = ConstraintSet()
        set.clone(binding.profileHeader)
        set.connect(
            binding.editButton.id,
            ConstraintSet.START,
            binding.profileHeader.id,
            ConstraintSet.START,
            params.height
        )

        set.applyTo(binding.profileHeader)

    }

}