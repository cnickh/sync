package daemon.dev.field.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import daemon.dev.field.R
import daemon.dev.field.databinding.FragmentChannelBinding
import daemon.dev.field.databinding.FragmentSelectChannelBinding
import daemon.dev.field.fragments.adapter.SelectAdapter

class ChannelSelectFragment  : Fragment() {
//    private val tempPost : TempPost by activityViewModels()

    private lateinit var binding: FragmentSelectChannelBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Retrieve and inflate the layout for this fragment
        binding = FragmentSelectChannelBinding.inflate(inflater, container, false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val channelAdapter = SelectAdapter()

        binding.channelList.adapter = channelAdapter
        binding.channelList.layoutManager = GridLayoutManager(requireContext(),2)

        val list = mutableListOf<String>()

//        for(c in PostRAM.channelList.value!!){
//            if(c != "all"){
//                list.add(c)
//            }
//        }
        channelAdapter.updateView(list)

        binding.done.setOnClickListener {

            val selected = channelAdapter.getSelected()
//            tempPost.setTargets(selected)
//            tempPost.create()
            parentFragmentManager.beginTransaction().remove(this).commit()

        }

        binding.close.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.container_view,ComposeFragment()).commit()
        }


    }

}