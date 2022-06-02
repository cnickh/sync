package daemon.dev.field.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import daemon.dev.field.R
import daemon.dev.field.databinding.FragmentProfileSelectBinding
import daemon.dev.field.fragments.adapter.DeviceAdapter

class ProfileSelectFragment : Fragment() {

    private lateinit var binding: FragmentProfileSelectBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chat.setOnClickListener {  }
        binding.add.setOnClickListener {  }
        binding.block.setOnClickListener {  }
        binding.back.setOnClickListener {
            activity?.supportFragmentManager?.commit {
                replace<ProfileFragment>(R.id.fragment_view)
                addToBackStack(null)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroyView() {
        super.onDestroyView()

    }


}