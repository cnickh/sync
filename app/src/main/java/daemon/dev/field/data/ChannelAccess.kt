package daemon.dev.field.data
import daemon.dev.field.cereal.objects.Channel
import daemon.dev.field.data.db.ChannelDao

class ChannelAccess(private val sync : ChannelDao) {

    val channels = sync.getChannels()

    fun clear(){
        sync.clear()
    }

    suspend fun delete(name : String){
        sync.delete(name)
    }

    suspend fun updateKey(name : String, key : String){
        sync.updateKey(name,key)
    }


    suspend fun createChannel(name : String, key : String){
        val ch = Channel(name, key)
        sync.insert(ch)
    }


    suspend fun key(name : String) : String {
        return sync.getKey(name)
    }


}