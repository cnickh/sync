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
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.R
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.databinding.FragmentProfileSelectBinding
import daemon.dev.field.fragments.dialogs.AddDialog
import daemon.dev.field.fragments.dialogs.BlockDialog
import daemon.dev.field.fragments.model.DialogModel
import daemon.dev.field.fragments.model.ResourceModel
import daemon.dev.field.fragments.model.SyncModel
import daemon.dev.field.network.Async
import daemon.dev.field.util.Phi
import kotlin.math.roundToInt


class ProfileSelectFragment : Fragment() {

    private val syncModel : SyncModel by activityViewModels()
    private val resModel : ResourceModel by activityViewModels()
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
        Log.v("ProfSel", "onCreate called")



        beautify()
        dialogModel.setUser(key)
        syncModel.getUser(key).observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                binding.username.text = user.alias
                binding.key.text = key
            } else {

            }
        })
        resModel.getUserProfile(key).observe(viewLifecycleOwner) {
            if (it != null) {
                val uri = it.toUri()

                Glide
                    .with(this)
                    .load(uri)
                    .circleCrop()
                    .thumbnail()
                    .placeholder(R.drawable.loading_spinner)
                    .into(binding.userImage);
            }
        }

        syncModel.channels.observeOnce(viewLifecycleOwner, Observer {

            Log.v("ProfSel", "have channels: $it")

            if (it.size == 1) {
                Log.v("ProfSel", "making add invisible")
                binding.add.visibility = View.INVISIBLE
            } else {
                Log.v("ProfSel", "making add visible")
                binding.add.visibility = View.VISIBLE
            }

        })

        binding.chat.setOnClickListener {

            val bundle = bundleOf("key" to key)

            val msgFrag = MessengerFragment()

            msgFrag.arguments = bundle

            val ft: FragmentTransaction =
                activity!!.supportFragmentManager.beginTransaction()


            ft.replace(R.id.container_view, msgFrag);
            ft.addToBackStack(null)
            ft.commit();

        }
        binding.add.setOnClickListener { add() }
        binding.block.setOnClickListener { block() }



        binding.back.setOnClickListener {
            //parentFragmentManager.popBackStack()
            parentFragmentManager.commit {
                replace<ProfileFragment>(R.id.fragment_view)
                addToBackStack(null)
            }
        }

        dialogModel.blockedStatus.observe(viewLifecycleOwner, Observer { status ->

            if (status == false) {
                val text = "User Blocked"
                val duration = Toast.LENGTH_SHORT

                val toast = Toast.makeText(requireContext(), text, duration)
                toast.show()

                syncModel.blockUser(key)
            }

        })

        Async.peers.observe(viewLifecycleOwner, Observer { peers ->

            val connected = peers.contains(User(key, "", 0, "",0))
            if (connected) {
                binding.connection.setBackgroundResource(R.drawable.circle_col)
            } else {
                binding.connection.setBackgroundResource(R.drawable.circle)
            }

        })
    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
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
            val itr = list.iterator()

            val stringList = mutableListOf<String>()

            while (itr.hasNext()){
                val next = itr.next()
                if(next.key != "null" && next.name != "Public"){
                    stringList.add(next.name)
                }
            }

            val array = ArrayList(stringList)

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

        val displayMetrics = requireContext().resources.displayMetrics
        val params = binding.profileCard.layoutParams
        val width = displayMetrics.widthPixels - (30*displayMetrics.density)
        params.width = width.roundToInt()
        val height = Phi().phi(width.roundToInt(),2)
        params.height = height


        val offset = ((params.height - 96*displayMetrics.density)/2).roundToInt()
//
//        val bParams = binding.buttonLayout.layoutParams
//        bParams.width = (width - 2*height).roundToInt()
//        binding.buttonLayout.layoutParams = bParams
//
        val set = ConstraintSet()
        set.clone(binding.profileCard)
//
//        val length = 36*3+32*2
//        val button_offset = (width.roundToInt() - height - length)/2
//
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
            binding.userImage.id,
            ConstraintSet.START,
            binding.profileCard.id,
            ConstraintSet.START,
            offset
        )
        set.connect(
            binding.buttonLayout.id,
            ConstraintSet.START,
            binding.profileCard.id,
            ConstraintSet.START,
            height
        )

        set.applyTo(binding.profileCard)


    }
}