package daemon.dev.field.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import daemon.dev.field.INBOX_TAG
import daemon.dev.field.R
import daemon.dev.field.data.PostRAM
import daemon.dev.field.databinding.FragmentInboxBinding
import daemon.dev.field.fragments.adapter.PostAdapter

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class InboxFragment : Fragment() {

    private lateinit var binding: FragmentInboxBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Retrieve and inflate the layout for this fragment
        binding = FragmentInboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val postAdapter = PostAdapter(view, requireActivity())

        binding.postInbox.adapter = postAdapter
        binding.postInbox.layoutManager = LinearLayoutManager(requireContext())

        PostRAM.postList.observe(viewLifecycleOwner, Observer { new_post_list ->
            Log.i(INBOX_TAG,"NEW LIST: $new_post_list")
            postAdapter.updateView(new_post_list)
        })


        binding.create.setOnClickListener {

            activity?.supportFragmentManager?.commit {
                replace<ComposeFragment>(R.id.container_view)
                addToBackStack(null)
            }

        }

    }


}