package daemon.dev.field.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.flexbox.*
import daemon.dev.field.PROFILE_TAG
import daemon.dev.field.data.PostRAM
import daemon.dev.field.databinding.FragmentProfileBinding
import daemon.dev.field.fragments.adapter.DeviceAdapter
import daemon.dev.field.network.PeerRAM
import daemon.dev.field.util.ServiceLauncher

class ProfileFragment : Fragment() {

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


        binding.mesh.isChecked = PostRAM.mesh

        binding.mesh.setOnCheckedChangeListener { mesh, _ ->
            if(mesh.isChecked){
                ServiceLauncher(view.context).checkStartMesh()
            } else if (!mesh.isChecked){
                ServiceLauncher(view.context).checkKillMesh()
            }
        }

        binding.alias.hint = PostRAM.getAlias()
        binding.signature.text = "sig: " + PostRAM.me.uid.toHex()
        deviceAdapter = DeviceAdapter(view, requireActivity())
        binding.userList.adapter = deviceAdapter

        //Black magic fuckary to center items in recycle view
        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.CENTER
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        binding.userList.layoutManager = layoutManager


        deviceAdapter.updateView(PeerRAM.activeUsers.value!!)

        PeerRAM.activeUsers.observe(viewLifecycleOwner) { userList ->
            Log.v(PROFILE_TAG,"Connected users updated: $userList")
            deviceAdapter.updateView(userList)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroyView() {
        super.onDestroyView()

        PostRAM.mesh = binding.mesh.isChecked

        if(binding.alias.text.toString() != "") {
            PostRAM.setAlias(binding.alias.text.toString())
        }
    }


}