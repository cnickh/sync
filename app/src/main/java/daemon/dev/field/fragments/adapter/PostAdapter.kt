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

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.recyclerview.widget.RecyclerView
import daemon.dev.field.INBOX_TAG
import daemon.dev.field.R
import daemon.dev.field.data.PostRAM
import daemon.dev.field.data.objects.Post
import daemon.dev.field.databinding.PostViewHolderBinding
import daemon.dev.field.fragments.PostFragment
import daemon.dev.field.network.PeerRAM


/**
 * Adapter for displaying remote Bluetooth devices that are being advertised
 */
@RequiresApi(Build.VERSION_CODES.O)
class PostAdapter(val view : View, val activity : FragmentActivity) : RecyclerView.Adapter<PostAdapter.PostVh>() {

    val TAG = "PostAdapter"

    private var itemsList: MutableList<Post> = arrayListOf()

    fun updateView(list:List<Post>){

        // Log.d(TAG, "updateView: called for itemlist size $size")

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
        holder.bind(getItem(position))
    }

    inner class PostVh(private val binding: PostViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Post?) {
            item?.let {

                binding.title.text = it.title
                binding.body.text = it.body
                binding.hopValue.text = it.hops.toString()
                Log.i(INBOX_TAG,"Have hops of ${it.hops}")

                var here = false
                for (i in PeerRAM.activeUsers.value!!){
                    if(i.cmp(item.user.uid)){
                        here = true
                    }
                }
                binding.radioButton.isChecked = here

                binding.postCard.setOnClickListener{ _ ->

                    val bundle = bundleOf("pid" to it.uid.toLong())

                    val postFrag = PostFragment()

                    postFrag.arguments = bundle

                    val ft : FragmentTransaction =
                        activity.supportFragmentManager.beginTransaction();

                    ft.replace(R.id.container_view, postFrag, "FRAGMENT_TAG");
                    ft.commit();

//                    activity.supportFragmentManager.commit {
//                        replace<PostFragment>(R.id.container_view)
//                        addToBackStack(null)
//                    }

                }

            }
        }

    }


}
