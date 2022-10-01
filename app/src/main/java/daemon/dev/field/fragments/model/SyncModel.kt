package daemon.dev.field.fragments.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.UNIVERSAL_KEY
import daemon.dev.field.cereal.objects.*
import daemon.dev.field.data.ChannelAccess
import daemon.dev.field.data.PostRepository
import daemon.dev.field.data.UserBase
import daemon.dev.field.network.Async
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

    val posts: LiveData<List<Post>> = postRepository.posts
    val peers: MutableLiveData<MutableList<User>> = Async.peers
    val channels = channelAccess.channels

    val state : LiveData<Int> = Async.live_state

    val new_thread = Async.new_thread
    val ping = Async.ping

    fun disconnect(user : User){
        viewModelScope.launch(Dispatchers.IO) {
            Async.disconnect(user)
        }
    }

    fun sendToTarget(raw : MeshRaw, key : String){
        viewModelScope.launch(Dispatchers.IO) {
            Async.send(raw, key)
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
            userBase.addChannel(string)
            channelAccess.createChannel(string, UNIVERSAL_KEY)
        }

    }


    fun create(
        title: String,
        body: String,
    ){
        val time = System.currentTimeMillis()

        val post = Post(PUBLIC_KEY,time,title,body,"null",0)

        //Packeting
        val data = hashMapOf<String,String>()
        data[post.address().address] = post.hash()
        val raw = MeshRaw(
            MeshRaw.NEW_DATA,
            null,
            null,
            data,
            null,
            null
        )

        viewModelScope.launch(Dispatchers.IO) {
            Log.i("SyncModel.kt","Created post $post")
            postRepository.add(post)
            Async.sendAll(raw)
        }

    }

    fun comment(position : Int, sub : MutableList<Comment>,globalSub : MutableList<Comment>, text : String) : Comment {

        val time = System.currentTimeMillis()

        val comment = Comment(PUBLIC_KEY,text,time)
        sub.add(comment)

        val post = get(position)!!
        post.comment = Json.encodeToString(globalSub)
        val data = hashMapOf<String,String>()
        data[post.address().address] = post.hash()
        val raw = MeshRaw(
            MeshRaw.NEW_DATA,
            null,
            null,
            data,
            null,
            null
        )

        viewModelScope.launch(Dispatchers.IO) {
            postRepository.stage(listOf(post))
            postRepository.commit()
            Async.sendAll(raw)
        }

        return comment
    }

}