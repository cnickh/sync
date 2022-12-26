package daemon.dev.field.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.R
import daemon.dev.field.cereal.objects.Comment
import daemon.dev.field.cereal.objects.Post
import daemon.dev.field.databinding.CommentViewHolderBinding
import daemon.dev.field.databinding.FragmentPostBinding
import daemon.dev.field.databinding.NewCommentViewHolderBinding
import daemon.dev.field.fragments.model.SyncModel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


@RequiresApi(Build.VERSION_CODES.O)
class PostFragment : Fragment() {
    private val syncModel : SyncModel by activityViewModels()
    private lateinit var binding: FragmentPostBinding
    private lateinit var post : Post
    private lateinit var globalSub : MutableList<Comment>

    private var pid : Int = 0

    private var commenting : Boolean = false
    private val maxDepth : Int = 4

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        pid = requireArguments().getInt("pid")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Retrieve and inflate the layout for this fragment
        binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("PostFragment.kt","Have $pid")
        post = syncModel.get(pid)!!

        binding.hops.text = post.hops.toString()
        binding.subjectText.text = post.title
        binding.bodyText.text = post.body

        syncModel.getUser(post.key).observe(viewLifecycleOwner, Observer { user ->
            if(user != null) {
                binding.author.text = user.alias
            }
        })

        syncModel.posts.observe(viewLifecycleOwner, Observer { _ ->
            Log.d("PostFragment.kt", "post_list changed signal received")
            post = syncModel.get(pid)!!
            binding.subComment.removeAllViews()
            Log.d("PostFragment.kt", "Updating thread with ${post.comment}")

            updateSub()
        })

//        var mediaPlayer = MediaPlayer.create(context, R.raw.shieldsup)
//        mediaPlayer.start()

        Log.d("PostFragment.kt", "comments ${post.comment}")

        binding.close.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commit()
        }

        updateSub()
    }

    private fun updateSub(){
        globalSub =
            if(post.comment!="null"){
                Json.decodeFromString(post.comment)
            }else{
                mutableListOf()
            }

        addComments(binding.subComment,globalSub,0)

        binding.comment.setOnClickListener {
            commentThis(globalSub, binding.subComment,0)
        }

    }


    private fun addComments(lLayout: LinearLayout, sub : MutableList<Comment>, depth : Int){

        for (c in sub){
            val card = CommentViewHolderBinding.inflate(LayoutInflater.from(context), null, false)
//            card.user.text = c.user.alias
            card.text.text = c.comment

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

            params.gravity = Gravity.END

            if(c.key != PUBLIC_KEY){
                card.commentCard.setBackgroundResource(R.drawable.cmt_left)
                params.gravity = Gravity.START
            }

            card.commentCard.setOnClickListener {
                commentThis(c.sub, card.subComment,depth+1)
            }

            addComments(card.subComment,c.sub,depth+1)

            lLayout.addView(card.root)

            card.root.layoutParams = params

        }

    }

    private fun commentThis(sub : MutableList<Comment>, lLayout: LinearLayout, depth : Int) {

        if (!commenting && depth <= maxDepth) {

            commenting = true
            binding.comment.isVisible = false

            val tempCard = NewCommentViewHolderBinding.inflate(LayoutInflater.from(context), null, false)

            tempCard.send.setOnClickListener {
                val mesg = tempCard.commentText.text.toString()
                Log.d("PostFragment.kt", "Created comment $mesg")

                commenting = false

                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm!!.hideSoftInputFromWindow(tempCard.root.windowToken, 0)

                binding.comment.isVisible = true
                binding.main.removeView(tempCard.root)

                val comment:String = tempCard.commentText.text.toString()

                val nwComment = syncModel.comment(pid,sub,globalSub,comment)
                val nwCard = CommentViewHolderBinding.inflate(LayoutInflater.from(context), null, false)

//                nwCard.user.text = comment.user.alias
                nwCard.text.text = nwComment.comment
                nwCard.commentCard.setOnClickListener {
                    commentThis(nwComment.sub, nwCard.subComment,depth+1)
                }
                lLayout.addView(nwCard.root)
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)

                params.gravity = Gravity.END

                nwCard.root.layoutParams = params
            }

            tempCard.root.autofillId
            binding.main.addView(tempCard.root)
            tempCard.commentCard.layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

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


            val et = tempCard.commentText
            et.requestFocus()
            val imm: InputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)

        }

    }

}