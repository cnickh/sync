package daemon.dev.field.network

import kotlinx.coroutines.sync.Mutex

object Sync {

    val open_channels = mutableListOf<String>()
    val channel_lock = Mutex()

    val broadcast_posts = mutableListOf<String>()
    val post_lock = Mutex()

    suspend fun openChannel(){

    }

    suspend fun closeChannel(){

    }

    suspend fun getOpen(){

    }

    suspend fun init_connection(key : String){

    }

}