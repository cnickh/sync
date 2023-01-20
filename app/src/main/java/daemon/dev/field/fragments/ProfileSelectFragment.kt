package daemon.dev.field.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.R
import daemon.dev.field.databinding.FragmentProfileSelectBinding
import daemon.dev.field.fragments.dialogs.AddDialog
import daemon.dev.field.fragments.dialogs.BlockDialog
import daemon.dev.field.fragments.model.DialogModel
import daemon.dev.field.fragments.model.SyncModel
import daemon.dev.field.util.Phi


class ProfileSelectFragment : Fragment() {

    private val syncModel : SyncModel by activityViewModels()
    private val dialogModel : DialogModel by viewModels()

    private lateinit var binding: FragmentProfileSelectBinding
    private lateinit var key: String

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        key = requireArguments().getString("key")!!
    }

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

//        syncModel.getUser(key).observe(viewLifecycleOwner, Observer { user ->
//            if(user != null) {
//                binding.username.text = user.alias
//                binding.key.text = key
//            }
//        })

        syncModel.channels.observeOnce(viewLifecycleOwner,Observer{

            Log.v("ProfSel","have channels: $it")

            if(it.size == 1 ){
                Log.v("ProfSel","making add invisible")
                binding.add.visibility = View.INVISIBLE
            }else{
                Log.v("ProfSel","making add visible")
                binding.add.visibility = View.VISIBLE
            }

        })

        binding.chat.setOnClickListener {

            val bundle = bundleOf("key" to key)

            val msgFrag = MessengerFragment()

            msgFrag.arguments = bundle

            val ft : FragmentTransaction =
                activity!!.supportFragmentManager.beginTransaction()


            ft.replace(R.id.container_view, msgFrag, "FRAGMENT_TAG");
            ft.addToBackStack(null)
            ft.commit();

        }
        binding.add.setOnClickListener { add() }
        binding.block.setOnClickListener { block() }



        binding.back.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        dialogModel.blockedStatus.observe(viewLifecycleOwner, Observer { status ->

            if(status == false){
                val text = "User Blocked"
                val duration = Toast.LENGTH_SHORT

                val toast = Toast.makeText(requireContext(), text, duration)
                toast.show()
            }

        })

    }
    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroyView() {
        super.onDestroyView()

    }

    fun add(){
        activity?.let {

            val list = syncModel.channels.value!!

            val array = ArrayList(list)
            array.remove("Public")

            val dialog = AddDialog(it,dialogModel,array)

            dialog.window?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT));

            dialog.window?.clearFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            dialog.show()

        }
    }

    fun block(){

        activity?.let {

            val dialog = BlockDialog(it,dialogModel)

            dialog.window?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT));

            dialog.window?.clearFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            dialog.show()

        }

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


                val bParams = binding.buttonLayout.layoutParams
                bParams.width = (width - 2*height)
                binding.buttonLayout.layoutParams = bParams

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