package daemon.dev.field.fragments

import android.content.Context
import android.media.MediaPlayer
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
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import daemon.dev.field.INBOX_TAG
import daemon.dev.field.R
import daemon.dev.field.data.PostRAM
import daemon.dev.field.data.objects.Comment
import daemon.dev.field.data.objects.Post
import daemon.dev.field.databinding.CommentViewHolderBinding
import daemon.dev.field.databinding.FragmentPostBinding
import daemon.dev.field.databinding.NewCommentViewHolderBinding


@RequiresApi(Build.VERSION_CODES.O)
class PostFragment : Fragment() {

    private lateinit var binding: FragmentPostBinding
    private lateinit var post : Post
    private var pid : ULong = 0u

    private var commenting : Boolean = false
    private val maxDepth : Int = 4

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        pid = requireArguments().getLong("pid").toULong()

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
        post = PostRAM.getPost(pid)!!

        binding.author.text = post.user.alias
        binding.hops.text = post.hops.toString()
        binding.subjectText.text = post.title
        binding.bodyText.text = post.body

        var mediaPlayer = MediaPlayer.create(context, R.raw.shieldsup)
        mediaPlayer.start()

        //ugly fix should improve |
        //                        V
        val placeholder = Comment(post.user,"place-holder",0,0)
        placeholder.commentList = post.comments

        addComments(binding.subComment,placeholder,0)

        binding.comment.setOnClickListener {
            commentThis(placeholder, binding.subComment,0)
        }

        binding.close.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commit()
        }

        PostRAM.newChat.observe(viewLifecycleOwner, Observer { uid ->

            if(uid == pid){

                binding.subComment.removeAllViews()

                //ugly fix should improve |
                //                        V
                val placeholder = Comment(post.user,"place-holder",0,0)
                post = PostRAM.getPost(pid)!!
                placeholder.commentList = post.comments
                addComments(binding.subComment,placeholder,0)

            }

        })
    }


    private fun addComments(lLayout: LinearLayout, comment : Comment, depth : Int){

        for (c in comment.commentList){
            val card = CommentViewHolderBinding.inflate(LayoutInflater.from(context), null, false)
//            card.user.text = c.user.alias
            card.text.text = c.comment

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

            params.gravity = Gravity.END

            if(!c.user.uid.cmp(comment.user.uid)){
                card.commentCard.setBackgroundResource(R.drawable.cmt_left)
                params.gravity = Gravity.START

            }

            card.commentCard.setOnClickListener {
                commentThis(c, card.subComment,depth+1)
            }

            addComments(card.subComment,c,depth+1)

            lLayout.addView(card.root)

            card.root.layoutParams = params

        }

    }

    private fun commentThis(comment : Comment, lLayout: LinearLayout, depth : Int) {

        if (!commenting && depth <= maxDepth) {

            commenting = true
            binding.comment.isVisible = false

            val tempCard = NewCommentViewHolderBinding.inflate(LayoutInflater.from(context), null, false)

            tempCard.send.setOnClickListener {
                val mesg = tempCard.commentText.text.toString()
                commenting = false

                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm!!.hideSoftInputFromWindow(tempCard.root.windowToken, 0)

                binding.comment.isVisible = true
                binding.main.removeView(tempCard.root)

                val nwComment = PostRAM.comment(post,comment.commentList,mesg,PostRAM.me)

                val nwCard = CommentViewHolderBinding.inflate(LayoutInflater.from(context), null, false)
//                nwCard.user.text = comment.user.alias
                nwCard.text.text = nwComment.comment
                nwCard.commentCard.setOnClickListener {
                    commentThis(nwComment, nwCard.subComment,depth+1)
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