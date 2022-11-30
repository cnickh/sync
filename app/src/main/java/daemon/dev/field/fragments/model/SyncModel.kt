package daemon.dev.field.fragments.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.UNIVERSAL_KEY
import daemon.dev.field.cereal.objects.*
import daemon.dev.field.data.ChannelAccess
import daemon.dev.field.data.PostRepository
import daemon.dev.field.data.UserBase
import daemon.dev.field.network.Async
import daemon.dev.field.network.Sync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**@brief this class provides database and network data to UI threads via coroutines.*/

class SyncModel internal constructor(
    private val postRepository: PostRepository,
    private val userBase: UserBase,
    private val channelAccess: ChannelAccess
) : ViewModel()  {

    val posts = postRepository.posts
    val peers = Async.peers
    val channels = channelAccess.channels
    val live_filter = Async.filter

    val state : LiveData<Int> = Async.live_state

    val new_thread = Async.new_thread
    val ping = Async.ping

    var filter : List<String>? = null

    fun filter(posts : List<Post>) : List<Post> {

        viewModelScope.launch(Dispatchers.IO) {
            filter = channelAccess.getOpenContents()
        }

        var ret = mutableListOf<Post>()

        Log.i("SyncModel","Have post list $posts")

        Log.i("SyncModel","filter is $filter")
        if(posts.isNotEmpty()){
            Log.i("SyncModel","address is ${posts[0].address().address}")
        }

        if(filter == null){
            ret = posts as MutableList<Post>
        }else{
            for(p in posts){
                if(filter!!.contains(p.address().address)){
                    ret.add(p)
                }
            }
        }
        Log.i("SyncModel","Have new list $ret")

        return ret
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
        viewModelScope.launch(Dispatchers.IO) {
           filter = channelAccess.getOpenContents()
            Log.i("SyncModel","Have filter $filter")
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

    fun addChannel(string : String){
        viewModelScope.launch(Dispatchers.IO){
            channelAccess.createChannel(string, UNIVERSAL_KEY)
        }
    }

    fun updateFilter(){
        viewModelScope.launch(Dispatchers.IO) {
            filter = channelAccess.getOpenContents()
        }
    }

    fun create(
        title: String,
        body: String,
    ){
        val time = System.currentTimeMillis()

        val post = Post(PUBLIC_KEY,time,title,body,"null",0)

        viewModelScope.launch(Dispatchers.IO) {
            Log.i("SyncModel.kt","Created post $post")
            postRepository.add(post)
            for(c in Sync.open_channels){
                channelAccess.addPost(c,post.address().address)
            }
            filter = channelAccess.getOpenContents()

        }

    }

    fun comment(position : Int, sub : MutableList<Comment>,globalSub : MutableList<Comment>, text : String) : Comment {

        val time = System.currentTimeMillis()

        val comment = Comment(PUBLIC_KEY,text,time)
        sub.add(comment)

        val post = get(position)!!
        post.comment = Json.encodeToString(globalSub)

        viewModelScope.launch(Dispatchers.IO) {
            postRepository.stage(listOf(post))
            postRepository.commit()
        }

        return comment
    }

}