package daemon.dev.field.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.transition.TransitionInflater
import daemon.dev.field.R
import daemon.dev.field.databinding.FragmentComposeBinding
import daemon.dev.field.fragments.model.SyncModel

class ComposeFragment : Fragment() {
    private val livePost : SyncModel by activityViewModels()

    lateinit var binding : FragmentComposeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_up)
        exitTransition = inflater.inflateTransition(R.transition.slide_up)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentComposeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.close.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commit()
        }

        binding.share.setOnClickListener {

            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)

            val body = binding.body.text.toString()
            val title = binding.subject.text.toString()
            livePost.create(title,body)

            exitTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.slide_down)
            parentFragmentManager.beginTransaction().remove(this).commit()
            parentFragmentManager.beginTransaction().replace(R.id.fragment_view,InboxFragment()).commit()
        }


    }

}
