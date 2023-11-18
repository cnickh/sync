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
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.flexbox.*
import daemon.dev.field.PROFILE_TAG
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.R
import daemon.dev.field.databinding.*
import daemon.dev.field.fragments.adapter.DeviceAdapter
import daemon.dev.field.fragments.adapter.UserAdapter
import daemon.dev.field.fragments.model.MessengerModel
import daemon.dev.field.fragments.model.ResourceModel
import daemon.dev.field.fragments.model.SyncModel
import daemon.dev.field.network.util.LoadController
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
    private val msgModel : MessengerModel by activityViewModels()

    private lateinit var binding: FragmentProfileBinding
    private lateinit var userAdapter : UserAdapter
    private lateinit var deviceAdapter : DeviceAdapter


    private var syncButton : SyncButtonBinding? = null
    private var readyButton : ReadyButtonBinding? = null

    private var uiState = "IDLE"
    private var alias = ""

    private lateinit var mServiceController : ServiceLauncher
    private lateinit var loadController : LoadController
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
    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mServiceController = syncModel.getServiceController()
        loadController = LoadController(mServiceController)
        beautify()

        setState(syncModel.state.value!!)

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

        syncModel.state.observe(viewLifecycleOwner) {
            setState(it)
        }

        syncModel.me.observe(viewLifecycleOwner) {
            alias = it?.alias ?: "No Alias!"
            binding.username.text = alias
            Log.i(PROFILE_TAG ,"We pulled user:$alias")
        }

        binding.stateFrame.setOnClickListener {

           CoroutineScope(Dispatchers.IO).launch {
               mServiceController.checkStartMesh(syncModel.me.value!!)
            }

        }

        binding.key.text = PUBLIC_KEY.toBase64()
        userAdapter = activity?.let { UserAdapter(view, it, loadController) }!!
        binding.userList.adapter = userAdapter

        //Black magic fuckary to center items in recycle view
        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.CENTER
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }

        binding.userList.layoutManager = layoutManager


        val lManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.CENTER
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }

        deviceAdapter = activity?.let { DeviceAdapter(view, it) }!!
        binding.deviceList.adapter = deviceAdapter
        binding.deviceList.layoutManager = lManager


        binding.clearPing.setOnClickListener {
            loadController.killLoad()
            userAdapter.notifyDataSetChanged()
        }

        syncModel.peers.observe(viewLifecycleOwner) { keys ->
            userAdapter.updateView(keys)
        }

        syncModel.devices.observe(viewLifecycleOwner) { dev ->
            deviceAdapter.updateView(dev)
        }
//
//        syncModel.ping.observe(viewLifecycleOwner) { _ ->
//            userAdapter.notifyDataSetChanged()
//        }

        msgModel.latest.observe(viewLifecycleOwner) { _ ->
            userAdapter.notifyDataSetChanged()
        }

        binding.editButton.setOnClickListener {
            editMode()
            Log.i("Fac-Debug" ,"entering edit mode set DONE")

        }

    }

    override fun onStop() {
        super.onStop()
        Log.i("Fac-Debug" ,"Prof onStop() called")
        loadController.killLoad()
    }

    private fun setState(state : String){
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

        card.username.hint = alias

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

                Log.i(PROFILE_TAG,"Setting alias $alias")

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

    private val getContent = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        //Handle the returned Uri
        val img = uri
        img?.let {
            resModel.setProfileImage(it)
        }

    }

    private fun idle(){

        when(uiState){
            "INSYNC"->{
                uiState = "IDLE"
                binding.userInfo.removeView(syncButton!!.stateFrame)
                binding.stateFrame.layoutParams = syncButton!!.stateFrame.layoutParams
                binding.userInfo.addView(binding.stateFrame)
                return
            }
            "READY"->{
                uiState = "IDLE"
                binding.userInfo.removeView(readyButton!!.stateFrame)
                binding.stateFrame.layoutParams = readyButton!!.stateFrame.layoutParams
                binding.userInfo.addView(binding.stateFrame)
                return
            }
            "IDLE"->{
                return
            }
        }

    }

    private fun ready(){

        if(readyButton == null){
            readyButton = ReadyButtonBinding.inflate(
                LayoutInflater.from(view!!.context),
                null,
                false
            )
            readyButton!!.stateFrame.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    mServiceController.checkKillMesh()
                }
            }
        }

        when(uiState){
            "INSYNC"->{
                uiState = "READY"
                binding.userInfo.removeView(syncButton!!.stateFrame)
                readyButton!!.stateFrame.layoutParams = syncButton!!.stateFrame.layoutParams
                binding.userInfo.addView(readyButton!!.stateFrame)
                return
            }
            "READY"->{
                return
            }
            "IDLE"->{
                uiState = "READY"
                binding.userInfo.removeView(binding.stateFrame)
                readyButton!!.stateFrame.layoutParams = binding.stateFrame.layoutParams
                binding.userInfo.addView(readyButton!!.stateFrame)
                return
            }
        }

    }

    private fun insync(){

        if(syncButton == null){
            syncButton = SyncButtonBinding.inflate(
                LayoutInflater.from(view!!.context),
                null,
                false
            )
            syncButton!!.stateFrame.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    mServiceController.checkKillMesh()
                }
            }
        }

        when(uiState){
            "INSYNC"->{
                return
            }
            "READY"->{
                uiState = "INSYNC"
                binding.userInfo.removeView(readyButton!!.stateFrame)
                syncButton!!.stateFrame.layoutParams = readyButton!!.stateFrame.layoutParams
                binding.userInfo.addView(syncButton!!.stateFrame)
                return
            }
            "IDLE"->{
                uiState = "INSYNC"
                binding.userInfo.removeView(binding.stateFrame)
                syncButton!!.stateFrame.layoutParams = binding.stateFrame.layoutParams
                binding.userInfo.addView(syncButton!!.stateFrame)
                return
            }
        }

    }

    private fun beautify(){

        val displayMetrics = requireContext().resources.displayMetrics
        val params = binding.profileHeader.layoutParams
        val width = displayMetrics.widthPixels - (30*displayMetrics.density)
        params.width = width.roundToInt()
        params.height = Phi().phi(width.roundToInt(),1)

    }

}