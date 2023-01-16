package daemon.dev.field.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import daemon.dev.field.R
import daemon.dev.field.databinding.DeviceViewHolderBinding
import daemon.dev.field.databinding.FragmentProfileSelectBinding
import daemon.dev.field.fragments.adapter.DeviceAdapter
import daemon.dev.field.util.Phi

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

        beautify()

        binding.chat.setOnClickListener { }
        binding.add.setOnClickListener { }
        binding.block.setOnClickListener { }
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


    private fun beautify() {

        binding.profileCard.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.profileCard.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val width = binding.profileCard.width
                val params = binding.profileCard.layoutParams

                val height = Phi().phi(width,2)
                params.height = height
                binding.profileCard.layoutParams = params


                val offset = (height - binding.frameLayout.width)/2

                val set = ConstraintSet()
                set.clone(binding.profileCard)

                set.connect(
                    binding.username.id,
                    ConstraintSet.START,
                    binding.profileCard.id,
                    ConstraintSet.START,
                    height
                )
                set.connect(
                    binding.key.id,
                    ConstraintSet.START,
                    binding.profileCard.id,
                    ConstraintSet.START,
                    height
                )

                set.connect(
                    binding.frameLayout.id,
                    ConstraintSet.START,
                    binding.profileCard.id,
                    ConstraintSet.START,
                    offset
                )

                set.connect(
                    binding.ref0.id,
                    ConstraintSet.START,
                    binding.profileCard.id,
                    ConstraintSet.START,
                    height
                )

                set.connect(
                    binding.ref1.id,
                    ConstraintSet.START,
                    binding.profileCard.id,
                    ConstraintSet.START,
                    (height*2)
                )

                set.applyTo(binding.profileCard)

                val params0 = binding.ref0.layoutParams
                params0.height = height
                binding.ref0.layoutParams = params0

                val params1 = binding.ref1.layoutParams
                params1.height = height
                binding.ref1.layoutParams = params1

            }
        })

    }
}