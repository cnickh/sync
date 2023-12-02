package daemon.dev.field.fragments.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import daemon.dev.field.*
import daemon.dev.field.cereal.objects.*
import daemon.dev.field.data.ChannelAccess
import daemon.dev.field.data.PostRepository
import daemon.dev.field.data.UserBase
import daemon.dev.field.nypt.ChannelBuilder
import daemon.dev.field.util.ServiceLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.system.exitProcess

/**@brief this class provides database and network data to UI threads via coroutines.*/

class SyncModel internal constructor(
    private val postRepository: PostRepository,
    private val userBase: UserBase,
    private val channelAccess: ChannelAccess
) : ViewModel()  {

    private val selectedChannels = mutableListOf<String>()
    val peers = MutableLiveData<List<User>>()
    val state = MutableLiveData("IDLE")

    var posts = postRepository.posts
    val channels = channelAccess.channels
    val me = userBase.getUser(PUBLIC_KEY.toBase64())

    private val pendingInfo = mutableListOf<Job>()
    private var openChannelMap = hashMapOf<String,MutableList<String>>()
    private lateinit var mServiceController : ServiceLauncher

    fun setServiceController(mServiceController : ServiceLauncher){
        this.mServiceController = mServiceController
    }

    fun getServiceController() : ServiceLauncher{
        return mServiceController
    }

    fun clearDB(){
        viewModelScope.launch(Dispatchers.IO) {
            userBase.clear()
            postRepository.clear()
            channelAccess.clear()
            channelAccess.createChannel("Public", UNIVERSAL_KEY)
        }
    }

    fun blockUser(key : String){
        viewModelScope.launch(Dispatchers.IO) {
            userBase.setUserStatus(key,1)
        }
    }

    fun createTagMap(){
        viewModelScope.launch(Dispatchers.IO) {

            openChannelMap = postRepository.mapOpenChannels(selectedChannels)

            Log.i("SYNC_MODEL", "Created map $openChannelMap")
        }
    }

    fun getChannels(address: Address) : List<String> {
        val channels = mutableListOf<String>()
        for ((c,l) in openChannelMap){
            if (l.contains(address.toString())){
                channels.add(c)
            }
        }
        return channels
    }

    fun selected(name : String) : Boolean {
        return selectedChannels.contains(name)
    }

    fun selectChannel(name : String) : Boolean{

        return if (name in selectedChannels){
            selectedChannels.remove(name)
            createTagMap()
            false
        } else {
            selectedChannels.add(name)
            createTagMap()
            buildInfo(null)
            Log.d("SyncModel","Setting selected Channels to $selectedChannels")
            posts = postRepository.getListPostFromChannelQuery(selectedChannels)
            true
        }
    }

    fun disconnect(key : String){
        mServiceController //TODO create service disconnect method
    }
    fun buildInfo(key : String?){

        if(key == null){
            for (j in pendingInfo){
                j.cancel()
            }
        }

        val open = selectedChannels

        val job = viewModelScope.launch(Dispatchers.IO) {
            delay(5000) //delay 5 seconds

            val info = userBase.wait(PUBLIC_KEY.toBase64())!!
            val channelInfo = hashMapOf<String, String>()
            val map = channelAccess.mapOpenChannels(open)

            for ((c, l) in map) {
                val hash = postRepository.hashListCombined(l)
                if (hash != "null") {
                    channelInfo[c] = postRepository.hashListCombined(l)
                }
            }

            if (channelInfo.isEmpty()){ return@launch }

            info.channels = Json.encodeToString(channelInfo)

            val raw = MeshRaw(MeshRaw.INFO, info, null, null, null, null)

            if (key == null){
                peers.value?.let{
                    for (p in it){
                        mServiceController.send(p.key,raw)
                    }
                }
            } else {
                mServiceController.send(key,raw)
            }
        }

        pendingInfo.add(job)
    }


    fun sendToTarget(raw : MeshRaw, key : String){
        mServiceController.send(key,raw)
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

        viewModelScope.launch(Dispatchers.IO) {

            if(selectedChannels.isEmpty()){
                Log.e("SyncModel.kt","Err no open channels, post not created")
                exitProcess(0)
            }

            postRepository.add(post)

            for(c in selectedChannels){
                postRepository.addPostToChannel(c, post.address())
            }
            buildInfo(null)
        }

    }

    fun comment(position : Int, sub : MutableList<Comment>, globalSub : MutableList<Comment>, text : String) : Comment {
        Log.v("SyncModel.kt", "Creating comment $text")

        val comment = Comment(PUBLIC_KEY.toBase64(), text, getTime())
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