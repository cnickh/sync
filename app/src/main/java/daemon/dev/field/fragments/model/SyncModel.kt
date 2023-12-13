package daemon.dev.field.fragments.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.PrimaryKey
import daemon.dev.field.*
import daemon.dev.field.cereal.objects.*
import daemon.dev.field.data.ChannelAccess
import daemon.dev.field.data.PostRepository
import daemon.dev.field.data.SyncInterface
import daemon.dev.field.data.UserBase
import daemon.dev.field.nypt.ChannelBuilder
import daemon.dev.field.util.ServiceLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.system.exitProcess

/**@brief this class provides database and network data to UI threads via coroutines.*/

class SyncModel internal constructor(
    private val si: SyncInterface,
) : ViewModel()  {

    private val selectedChannels = mutableListOf<String>()
    val peers = MutableLiveData<List<User>>()
    val state = MutableLiveData("IDLE")

    val posts =  {
        Log.v("SyncModel.kt", "postList updateCalled")
        si.getListPostFromChannelQuery(selectedChannels)
    }
    val channels = si.channels
    val me = si.me

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
            si.clearDB()
        }
    }

    fun blockUser(key : String){
        viewModelScope.launch(Dispatchers.IO) {
            Log.i(SYNC_TAG, "Setting status")
            si.setUserStatus(key,User.BLOCKED)

            for (u in si.users()){
                Log.i(SYNC_TAG, u.print())
            }

            mServiceController.notify(key)
        }
    }

    private fun createTagMap(){
        viewModelScope.launch(Dispatchers.IO) {
            openChannelMap = si.map(selectedChannels)
            Log.i("SYNC_MODEL", "Created map $openChannelMap")
        }
    }

    fun getChannels(address: Address) : List<String> {
        val channels = mutableListOf<String>()
        for ((c,l) in openChannelMap){
            if (l.contains(address.address)){
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
            true
        }
    }

    fun disconnect(key : String){
        mServiceController //TODO create service disconnect method
    }

    fun handleInfo(user : User){
        val open = selectedChannels
        CoroutineScope(Dispatchers.IO).launch {
            si.handleInfo(user,open)?.let{
                mServiceController.send(user.key,it)
            }
        }
    }

    fun buildInfo(key : String?){
        if(key == null){
            for (j in pendingInfo){
                j.cancel()
            }
        }
        val open = selectedChannels
        val job = viewModelScope.launch(Dispatchers.IO) {
            Log.i("INFO.kt", "job Delayed")
            delay(5000) //delay 5 seconds
            Log.i("INFO.kt", "job Started")
            si.buildInfo(open).let{ raw ->
                if (key == null){
                    mServiceController.send("null",raw)
                } else {
                    mServiceController.send(key,raw)
                }
            }
        }
        pendingInfo.add(job)
        job.start()
    }


    fun sendToTarget(raw : MeshRaw, key : String){
        mServiceController.send(key,raw)
    }

    fun setAlias(alias : String){
        viewModelScope.launch(Dispatchers.IO) {
            si.setAlias(alias)
        }
    }

    fun getUser(key : String) : LiveData<User> {
        return si.getUser(key)
    }

    fun get(position : Int) : LiveData<Post>{
        return si.get(position)
    }

    fun removeChannel(name : String){
        viewModelScope.launch(Dispatchers.IO) {
            si.removeChannel(name)
        }
    }

    fun buildChannel(name : String) {
        viewModelScope.launch(Dispatchers.IO) {
            si.buildChannel(name)
        }
    }

    fun addChannel(name : String){
        viewModelScope.launch(Dispatchers.IO) {
            si.addChannel(name)
        }
    }

    fun create(
        title: String,
        body: String,
    ){
        viewModelScope.launch(Dispatchers.IO) {
             si.create(title,body,selectedChannels)?.let{
                 openChannelMap = it
                 buildInfo(null)
            }
        }
    }

    fun comment(position : Int, sub : MutableList<Comment>, globalSub : MutableList<Comment>, text : String){
        viewModelScope.launch(Dispatchers.IO) {
            si.comment(position,sub,globalSub,text)
            buildInfo(null)
        }
    }
}