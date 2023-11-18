/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package daemon.dev.field.fragments.adapter

import android.graphics.Paint
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.R
import daemon.dev.field.cereal.objects.Post
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.databinding.PostViewHolderBinding
import daemon.dev.field.databinding.SingleChipLayoutBinding
import daemon.dev.field.fragments.PostFragment
import daemon.dev.field.fragments.model.SyncModel
import daemon.dev.field.util.Phi
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


/**
 * Adapter for displaying remote Bluetooth devices that are being advertised
 */
@RequiresApi(Build.VERSION_CODES.O)
class PostAdapter(val activity : FragmentActivity, val syncModel: SyncModel) : RecyclerView.Adapter<PostAdapter.PostVh>() {

    val TAG = "PostAdapter"

    private var itemsList: MutableList<Post> = arrayListOf()

    fun updateView(list:List<Post>){

        val mutableList = list as MutableList<Post>

        if(mutableList != itemsList){
            itemsList = mutableList
        }
        notifyDataSetChanged()

    }

    override fun getItemCount() = itemsList.size

    private fun getItem(position: Int): Post? = if (itemsList.isEmpty()) null else itemsList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostVh {
        val binding =
            PostViewHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostVh(binding)
    }

    override fun onBindViewHolder(holder: PostVh, position: Int) {
        holder.bind(position)
    }

    inner class PostVh(private val binding: PostViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val item = getItem(position)
            item?.let {

                val post = it

                binding.title.text = it.title

                if(it.body.length > 100){
                    binding.body.text = it.body.substring(0,99) + "..."
                }else{
                    binding.body.text = it.body
                }

                binding.hopValue.text = it.hops.toString()
                syncModel.peers.value?.let {

                    val connected = it.contains(User(post.key, "", 0, ""))

                    if(connected) {
                        binding.hopValue.setBackgroundResource(R.drawable.circle_col)
                    }else if(post.key == PUBLIC_KEY.toBase64()){
                        binding.hopValue.setBackgroundResource(R.drawable.circle_col)
                    } else {
                        binding.hopValue.setBackgroundResource(R.drawable.circle)
                    }
                }

                binding.timeStamp.text = getDateTime(it.time)
//                binding.channels.removeAllViews()
//                val key = post.address().address
//                Log.i("POST_ADAPTER","does $key have channels \n ${syncModel.postChannelMap}")
//
//                for (channel in syncModel.postChannelMap[key]!!){
//                    Log.i("POST_ADAPTER","HAVE $channel")
//                    val chip = SingleChipLayoutBinding.inflate(LayoutInflater.from(binding.channels.context), binding.channels, false)
//                    chip.channel.text = channel
//                    binding.channels.addView(chip.root)
//                }


                binding.postCard.setOnClickListener{ _ ->

                    val bundle = bundleOf("pid" to position)

                    val postFrag = PostFragment()

                    postFrag.arguments = bundle

                    val ft : FragmentTransaction =
                        activity.supportFragmentManager.beginTransaction();

                    ft.replace(R.id.container_view, postFrag, "FRAGMENT_TAG");
                    ft.commit();

                }
                beautify(binding)

                val anim = AnimationUtils.loadAnimation(activity,R.anim.post_anim)
                binding.postCard.startAnimation(anim)
//                binding.postCard.setOnTouchListener(SlideView(binding.postCard))
            }
        }

    }

    fun getDateTime(s: Long): String? {
        return try {
            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm:ss") 
            val netDate = Date(s)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

    private fun beautify(binding: PostViewHolderBinding){

        Log.v("PostAdapter.kt","beautify called on ${binding.title.text}")

        val displayMetrics = activity.resources.displayMetrics
        val width = displayMetrics.widthPixels - (30*displayMetrics.density)
        val height = Phi().phi(width.roundToInt(),4)

        val params = binding.body.layoutParams
        params.height = height - binding.title.height - binding.timeStamp.height
        binding.body.layoutParams = params

        val cardParams = binding.postCard.layoutParams
        cardParams.width = width.roundToInt()
        binding.postCard.layoutParams = cardParams

    }

    private fun isTooLarge(text: TextView, newText: String): Boolean {
        val textWidth = text.paint.measureText(newText)

        val fm: Paint.FontMetrics =  text.paint.fontMetrics
        val textHeight: Float = fm.descent - fm.ascent

        return textWidth >= text.measuredWidth
    }

    private fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
    }
}
