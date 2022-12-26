package daemon.dev.field.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import daemon.dev.field.INBOX_TAG
import daemon.dev.field.R
import daemon.dev.field.databinding.FragmentInboxBinding
import daemon.dev.field.fragments.adapter.PostAdapter
import daemon.dev.field.fragments.model.SyncModel

class InboxFragment : Fragment() {

    private val sync : SyncModel by activityViewModels()

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

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val postAdapter = PostAdapter(requireActivity())

        binding.postInbox.adapter = postAdapter
        binding.postInbox.layoutManager = LinearLayoutManager(requireContext())

        sync.posts.observe(viewLifecycleOwner, Observer { new_post_list ->
            Log.d(INBOX_TAG,"Observe post_list fired on \n $new_post_list")
            postAdapter.updateView(sync.filter(new_post_list))
        })

        sync.peers.observe(viewLifecycleOwner, Observer { _ ->
            postAdapter.notifyDataSetChanged()
        })

        sync.raw_filter.observe(viewLifecycleOwner, Observer {
            Log.d(INBOX_TAG,"Observe raw_filter fired on \n $it")
            sync.updateFilter(it)
            postAdapter.updateView(sync.filter(null))
        })

        binding.create.setOnClickListener {

            activity?.supportFragmentManager?.commit {
                replace<ComposeFragment>(R.id.container_view)
                addToBackStack(null)
            }

        }

    }


}