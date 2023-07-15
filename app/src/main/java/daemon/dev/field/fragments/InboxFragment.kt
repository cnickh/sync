package daemon.dev.field.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import com.google.android.flexbox.*
import daemon.dev.field.INBOX_TAG
import daemon.dev.field.databinding.FragmentInboxBinding
import daemon.dev.field.fragments.adapter.PostAdapter
import daemon.dev.field.fragments.model.SyncModel
import daemon.dev.field.R


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

        val postAdapter = PostAdapter(requireActivity(),sync)

        binding.postInbox.adapter = postAdapter
        //binding.postInbox.layoutManager = LinearLayoutManager(requireContext())

        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.CENTER
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }

        binding.postInbox.layoutManager = layoutManager

        sync.posts.observe(viewLifecycleOwner, Observer { new_post_list ->
            Log.d(INBOX_TAG,"Observe post_list fired on \n $new_post_list")
            sync.createTagMap()
            postAdapter.updateView(sync.filter(new_post_list))
            postAdapter.notifyDataSetChanged()
        })

        sync.peers.observe(viewLifecycleOwner, Observer { _ ->
            postAdapter.notifyDataSetChanged()
        })

        sync.raw_filter.observe(viewLifecycleOwner, Observer {
            Log.d(INBOX_TAG,"Observe raw_filter fired on \n $it")
            sync.createTagMap()
            postAdapter.updateView(sync.filter(null))
            postAdapter.notifyDataSetChanged()
        })

        binding.create.setOnClickListener {

            activity?.supportFragmentManager?.commit {
                replace<ComposeFragment>(R.id.container_view)
                addToBackStack(null)
            }
//            val sr = SecureRandom()
//            val len = sr.nextInt() % 300
//            val body = RandomString(len.absoluteValue+5).nextString()
//            val title = RandomString(10).nextString()
//            sync.create(title, body)
        }

    }


}