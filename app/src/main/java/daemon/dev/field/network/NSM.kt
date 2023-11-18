package daemon.dev.field.network

import kotlinx.coroutines.sync.Mutex

object NSM {

    val channels = HashMap<String,String>()
    val channel_lock = Mutex()

    suspend fun channelInfo(key : String) : String{
        return "String"
    }

    suspend fun getOpenChannels() : List<String> {
        return listOf()
    }

    suspend fun selectChannel(name : String){

    }

}