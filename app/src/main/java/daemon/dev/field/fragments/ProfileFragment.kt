package daemon.dev.field.fragments

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.flexbox.*
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.databinding.EditProfileBinding
import daemon.dev.field.databinding.FragmentProfileBinding
import daemon.dev.field.fragments.adapter.DeviceAdapter
import daemon.dev.field.fragments.model.SyncModel
import daemon.dev.field.network.Async
import daemon.dev.field.util.Phi


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

        beautify()

        syncModel.state.observe(viewLifecycleOwner) { state ->

            binding.state.text = Async.state2String()

        }

        binding.stateFrame.setOnClickListener {

//           CoroutineScope(Dispatchers.IO).launch {
//                if (Async.state() == Async.IDLE) {
//                    ServiceLauncher(view.context).checkStartMesh()
//                } else if (Async.state() != Async.IDLE) {
//                    ServiceLauncher(view.context).checkKillMesh()
//                }
//            }
//
//            binding.state.text = Async.state2String()
            val keys = listOf<User>(

                User("ALKFDJ)@#{P","TonyJboyz",0,"None"),
                User("ALKFDJ)@#{P","Tim Halaberton",0,"None"),
                User("ALKFDJ)@#{P","HQ_yamean",0,"None")

            )


            Log.d("ProfileFragment.kt", "defuq boi")
            deviceAdapter.updateView(keys)
        }

        binding.key.text = PUBLIC_KEY
        deviceAdapter = activity?.let { DeviceAdapter(view, it) }!!
        binding.userList.adapter = deviceAdapter

        //Black magic fuckary to center items in recycle view
        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.CENTER
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        binding.userList.layoutManager = layoutManager

        syncModel.peers.observe(viewLifecycleOwner) { keys ->
            deviceAdapter.updateView(keys)
        }

        binding.editButton.setOnClickListener {
            editMode()
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun editMode(){

        val card = EditProfileBinding.inflate(
            LayoutInflater.from(view?.context),
            null,
            false
        )

        binding.base.removeView(binding.profileHeader)
        binding.base.addView(card.root)

        card.root.layoutParams = binding.profileHeader.layoutParams
        card.doneButton.layoutParams = binding.editButton.layoutParams
        card.userInfo1.layoutParams = binding.userInfo.layoutParams

        val old_width = binding.editButton.width
        val old_height = binding.editButton.height

        card.doneButton.updateLayoutParams {
            width = LayoutParams.WRAP_CONTENT
            height = LayoutParams.WRAP_CONTENT
        }

        card.doneButton.setOnTouchListener(OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->                 // PRESSED
                    card.doneButton.setTextColor(Color.MAGENTA)

                MotionEvent.ACTION_UP ->                 // RELEASED
                    card.doneButton.setTextColor(Color.BLACK)
            }
            false
        })



        card.doneButton.setOnClickListener {
            binding.base.removeView(card.root)
            binding.base.addView(binding.profileHeader)

            binding.profileHeader.layoutParams = card.root.layoutParams
            binding.editButton.layoutParams = card.doneButton.layoutParams
            binding.userInfo.layoutParams = card.userInfo1.layoutParams


            binding.editButton.updateLayoutParams {
                width = old_width
                height = old_height
            }
        }

    }

    private fun beautify(){

        view!!.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view!!.viewTreeObserver.removeOnGlobalLayoutListener(this)



                val params = binding.profileHeader.layoutParams
                params.width = binding.profileHeader.width
//                Log.d("ProfileFragment.kt", "width1: ${params.width}")

                params.height = Phi().phi(params.width,1)
                binding.profileHeader.layoutParams = params

                val editConstraint = (params.width - params.height)//Phi().pxToDp(params.width - params.height, requireContext())

                Log.d("ProfileFragment.kt", "width: ${params.width} \n" +
                        "height: ${params.height} \n" +
                       "buttonWidth: ${binding.editButton.width} \n " +
                        "editConstraint: $editConstraint")


                //set constraints to comment writer
                val set = ConstraintSet()
                set.clone(binding.profileHeader)
                set.connect(
                    binding.editButton.id,
                    ConstraintSet.START,
                    binding.profileHeader.id,
                    ConstraintSet.START,
                    params.height-4
                )

                set.applyTo(binding.profileHeader)



                val userHeight = binding.userInfo.height
                val rem = params.height - userHeight

                Log.d("ProfileFragment.kt", "rem: $rem \n" +
                "userHeight: $userHeight")


                val set0 = ConstraintSet()
                set0.clone(binding.profileHeader)
                set0.connect(
                    binding.userInfo.id,
                    ConstraintSet.TOP,
                    binding.profileHeader.id,
                    ConstraintSet.TOP,
                    rem/2
                )
                set0.connect(
                    binding.editButton.id,
                    ConstraintSet.TOP,
                    binding.profileHeader.id,
                    ConstraintSet.TOP,
                    rem/2
                )

                set0.applyTo(binding.profileHeader)


                val params0 = binding.ref.layoutParams
                params0.height = params.height
                binding.ref.layoutParams = params0

                //set constraints to comment writer
                val nset = ConstraintSet()
                nset.clone(binding.profileHeader)
                nset.connect(
                    binding.ref.id,
                    ConstraintSet.END,
                    binding.profileHeader.id,
                    ConstraintSet.END,
                    editConstraint
                )

                nset.applyTo(binding.profileHeader)

            }
        })

    }

}