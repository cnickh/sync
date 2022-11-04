package daemon.dev.field.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.flexbox.*
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.databinding.FragmentProfileBinding
import daemon.dev.field.fragments.adapter.DeviceAdapter
import daemon.dev.field.fragments.model.SyncModel
import daemon.dev.field.network.Async
import daemon.dev.field.util.ServiceLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ProfileFragment : Fragment() {

    private val syncModel : SyncModel by activityViewModels()

    private lateinit var binding: FragmentProfileBinding
    private lateinit var deviceAdapter : DeviceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        syncModel.state.observe(viewLifecycleOwner) { state ->

            binding.mesh.isChecked = (state != Async.IDLE)

        }

        binding.mesh.setOnCheckedChangeListener { mesh, _ ->

            CoroutineScope(Dispatchers.IO).launch {
                if (mesh.isChecked) {
                    ServiceLauncher(view.context).checkStartMesh()
                } else if (!mesh.isChecked) {
                    ServiceLauncher(view.context).checkKillMesh()
                }
            }

        }

        binding.signature.text = PUBLIC_KEY
        deviceAdapter = DeviceAdapter(view,syncModel)
        binding.userList.adapter = deviceAdapter

        //Black magic fuckary to center items in recycle view
        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.CENTER
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        binding.userList.layoutManager = layoutManager

        syncModel.getUser(PUBLIC_KEY).observe(viewLifecycleOwner) { user ->
//            Log.v("Prof", "$user retrieved")
            if(user != null){
                binding.alias.hint = user.alias
            }
        }


        binding.alias.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_PREVIOUS -> {

                    val alias = binding.alias.text.toString()
                    if(alias != "") syncModel.setAlias(binding.alias.text.toString())

                    return@OnEditorActionListener true
                }
            }
            false
        })




        syncModel.peers.observe(viewLifecycleOwner) { keys ->
            deviceAdapter.updateView(keys)
        }

    }

}