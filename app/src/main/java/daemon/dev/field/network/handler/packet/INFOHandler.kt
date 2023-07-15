package daemon.dev.field.network.handler.packet

import android.util.Log
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.data.ChannelAccess
import daemon.dev.field.data.PostRepository
import daemon.dev.field.data.UserBase
import daemon.dev.field.network.Socket
import daemon.dev.field.network.Sync
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class INFOHandler(val userBase : UserBase,
                  val channelAccess : ChannelAccess,
                  val postRepository : PostRepository) {

//i
    suspend fun handle( raw : MeshRaw, socket: Socket){
    Log.i("INFO.kt","Received INFO")


    val info = raw.nodeInfo!!
        val channels = Json.decodeFromString<HashMap<String,String>>(info.channels)
        info.channels = "null"
        userBase.update(info)

        val dif = mutableListOf<String>()

        Log.i("INFO.kt", "Receive peer channels $channels")
        for ((c,h) in channels){
            if(Sync.channelInfo(c) != h){
                dif.add(c)
            }
        }
        Log.i("INFO.kt","Have dif $dif")
        if(dif.isNotEmpty()){
            val channel_to_list_of_post_address = channelAccess.resolveChannels(dif)

            Log.i("INFO.kt","Have channel_to_list_of_post_address $channel_to_list_of_post_address")

            if(channel_to_list_of_post_address.isNotEmpty()) {
                val data_map = hashMapOf<String, HashMap<String, String>>()

                for ((channel, list) in channel_to_list_of_post_address) {
                    val hash = postRepository.hashList(list)
                    data_map[channel] = hash
                }

                val newRaw = MeshRaw(MeshRaw.NEW_DATA, null, null, data_map, null, null)
                //Async.send(raw,socket)
                Sync.queue(socket.key, newRaw)
            }
        }
    }

}