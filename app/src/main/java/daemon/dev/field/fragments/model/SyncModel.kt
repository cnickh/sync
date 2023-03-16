package daemon.dev.field.fragments.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import daemon.dev.field.CHARSET
import daemon.dev.field.MODEL_TAG
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.UNIVERSAL_KEY
import daemon.dev.field.cereal.objects.*
import daemon.dev.field.data.ChannelAccess
import daemon.dev.field.data.PostRepository
import daemon.dev.field.data.UserBase
import daemon.dev.field.network.Async
import daemon.dev.field.network.Sync
import daemon.dev.field.nypt.ChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

/**@brief this class provides database and network data to UI threads via coroutines.*/

class SyncModel internal constructor(
    private val postRepository: PostRepository,
    private val userBase: UserBase,
    private val channelAccess: ChannelAccess
) : ViewModel()  {

    val posts = postRepository.posts
    val peers = Async.peers
    val channels = channelAccess.channels
    var raw_filter = channelAccess.getLiveContents(Sync.open_channels)
    val state : LiveData<Int> = Async.live_state
    val me = userBase.getUser(PUBLIC_KEY.toBase64())

    val ping = Async.ping

    private var filter : List<String> = listOf()
    
    fun filter(posts : List<Post>?) : List<Post> {

        raw_filter.value?.let{ updateFilter(it) }

        val ret = mutableListOf<Post>()

        if(posts != null){
            Log.d(MODEL_TAG,"filter posts: $posts")
            Log.d(MODEL_TAG,"filter: $filter")
            for (p in posts) {
                if (filter.contains(p.address().address)) {
                    ret.add(p)
                }
            }
        }else{
            val posts_last = this.posts.value ?: return ret
            Log.d(MODEL_TAG,"filter posts_last: $posts_last")
            Log.d(MODEL_TAG,"filter: $filter")

            for (p in posts_last) {
                if (filter.contains(p.address().address)) {
                    ret.add(p)
                }
            }
        }

        Log.d(MODEL_TAG,"filter ret: $ret")
        return ret
    }

    fun updateFilter(content : List<String>){

        val posts = mutableListOf<String>()

        for(c in content){
            
            if(c!="null") {
                for (p in c.split(",")) {
                    if (!posts.contains(p)) posts.add(p)
                }
            }

        }

        filter = posts
    }

    fun listContent2Log(channels : List<String>) {
        viewModelScope.launch(Dispatchers.IO){
            for (c in channels){
                val contents = channelAccess.waitContents(c)
                Log.i("SyncModel","$c :: $contents")
            }
        }
    }

    fun selectChannel(name : String) : Boolean {
        val ret = Sync.selectChannel(name)
        raw_filter = channelAccess.getLiveContents(Sync.open_channels)
        viewModelScope.launch(Dispatchers.IO) {
            Sync.queueUpdate()
        }
        return ret
    }

    fun disconnect(user : User){
        viewModelScope.launch(Dispatchers.IO) {
            Async.disconnect(user)
        }
    }

    fun sendToTarget(raw : MeshRaw, key : String){
        viewModelScope.launch(Dispatchers.IO) {
//            Async.send(raw, key)
            Sync.queue(key,raw)
        }
    }

    fun setAlias(alias : String){
        viewModelScope.launch(Dispatchers.IO) {
            userBase.setAlias(alias)
        }
    }

    fun getUser(key : String) : LiveData<User> {
        return userBase.getUser(key)
    }

    fun get(position : Int) : Post?{
        return posts.value?.get(position)
    }

    fun removeChannel(name : String){
        viewModelScope.launch(Dispatchers.IO) {
            channelAccess.delete(name)
        }
    }

    fun buildChannel(name : String) {
        val builder = ChannelBuilder(name)
        viewModelScope.launch(Dispatchers.IO) {
            channelAccess.updateKey(name,builder.key().toBase64())
        }
    }

    fun addChannel(name : String){
        val builder = ChannelBuilder(name)

        viewModelScope.launch(Dispatchers.IO) {
            channelAccess.createChannel(name,builder.key().toBase64())
        }
    }

    fun create(
        title: String,
        body: String,
    ){
        val time = System.currentTimeMillis()

        val post = Post(PUBLIC_KEY.toBase64(),time,title,body,"null",0)

        if(Sync.open_channels.isEmpty()){
            Log.e("SyncModel.kt","Err no open channels, post not created")
            return
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            Log.v("SyncModel.kt","Created post $post")
            postRepository.add(post)
            for(c in Sync.open_channels){
                channelAccess.addPost(c,post.address().address)
            }
            filter = channelAccess.getOpenContents()
            Sync.queueUpdate()
        }

    }

    fun comment(position : Int, sub : MutableList<Comment>, globalSub : MutableList<Comment>, text : String) : Comment {

        Log.v("SyncModel.kt","Creating comment $text")


        val time = System.currentTimeMillis()

        val comment = Comment(PUBLIC_KEY.toBase64(),text,time)
        sub.add(comment)

        val post = get(position)!!
        post.comment = Json.encodeToString(globalSub)

        viewModelScope.launch(Dispatchers.IO) {
            postRepository.stage(listOf(post))
            postRepository.commit()
            Sync.queueUpdate()
        }

        return comment
    }
    private fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
    }

    private fun String.toByteArray() : ByteArray {
        return Base64.getDecoder().decode(this)
    }
}