package daemon.dev.field.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import daemon.dev.field.databinding.CommentViewHolderBinding
import daemon.dev.field.databinding.MessageBinding
import daemon.dev.field.databinding.MessengerFragmentBinding
import daemon.dev.field.databinding.NewCommentViewHolderBinding
import daemon.dev.field.fragments.model.MessengerModel

class MessengerFragment: Fragment() {
    private val messenger: MessengerModel by activityViewModels()

    private lateinit var binding: MessengerFragmentBinding

    private lateinit var key: String


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        key = requireArguments().getString("key")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Retrieve and inflate the layout for this fragment
        binding = MessengerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //setUpTextBox()
        binding.send.setOnClickListener {

            val mesg = binding.messageText.text.toString()

//            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
//            imm!!.hideSoftInputFromWindow(binding.root.windowToken, 0)

            val nwCard = MessageBinding.inflate(LayoutInflater.from(context), null, false)
            nwCard.text.text = mesg

            binding.something.addView(nwCard.root)
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

            params.gravity = Gravity.END

            nwCard.root.layoutParams = params

            binding.messageText.setText("")
        }

        binding.close.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commit()
        }

    }

    private fun setUpTextBox() {

        val tempCard =
            NewCommentViewHolderBinding.inflate(LayoutInflater.from(context), null, false)

        tempCard.send.setOnClickListener {

        }

        tempCard.root.autofillId
        binding.main.addView(tempCard.root)
        tempCard.commentCard.layoutParams = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        //set constraints to comment writer
        val set = ConstraintSet()
        set.clone(binding.main)
        set.connect(
            tempCard.root.id,
            ConstraintSet.BOTTOM,
            binding.main.id,
            ConstraintSet.BOTTOM,
            0
        )

        set.applyTo(binding.main)

    }

}
