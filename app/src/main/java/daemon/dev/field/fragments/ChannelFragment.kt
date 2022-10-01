package daemon.dev.field.fragments

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import daemon.dev.field.databinding.FragmentChannelBinding
import daemon.dev.field.fragments.adapter.ChannelAdapter
import daemon.dev.field.fragments.model.SyncModel

class ChannelFragment : Fragment() {

    private val syncModel : SyncModel by activityViewModels()

    private lateinit var binding: FragmentChannelBinding

    override fun onCreate(savedInstanceState: Bundle?){ super.onCreate(savedInstanceState) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Retrieve and inflate the layout for this fragment
        binding = FragmentChannelBinding.inflate(inflater, container, false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val channelAdapter = ChannelAdapter(view)

        binding.channelList.adapter = channelAdapter
        binding.channelList.layoutManager = GridLayoutManager(requireContext(),2)

        syncModel.channels.observe(viewLifecycleOwner, Observer { list ->

            channelAdapter.updateView(list)

        })

        binding.create.text="Create Channel"

        binding.create.setOnClickListener {
            createBin(requireContext())
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createBin(context: Context) {
        val taskEditText = EditText(context)
        taskEditText.hint = "Bin Name"
        val dialog: AlertDialog = AlertDialog.Builder(context)
            .setTitle("Enter Name")
            .setView(taskEditText)
            .setPositiveButton("Create",
                DialogInterface.OnClickListener { a0, a1 -> /*create channel*/

                    syncModel.addChannel(taskEditText.text.toString())

                })
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

}
