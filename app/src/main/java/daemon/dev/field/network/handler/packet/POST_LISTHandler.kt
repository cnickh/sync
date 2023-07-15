package daemon.dev.field.network.handler.packet

import android.util.Log
import daemon.dev.field.CHARSET
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.data.ChannelAccess
import daemon.dev.field.data.PostRepository
import daemon.dev.field.network.Sync
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.lang.Thread.sleep
import java.util.*
import kotlin.collections.HashMap

class POST_LISTHandler(val postRepository: PostRepository, val channelAccess: ChannelAccess) {

    suspend fun handle(raw: MeshRaw){
//d
        Log.d("POST_LIST.kt","Received POST_LIST")
        for(p in raw.posts!!){
            p.index=0
            p.hops++
            postRepository.add(p)
        }

        raw.misc?.let{
            val json = it
            val meta = Json.decodeFromString<HashMap<String,List<String>>>(json)

            for((c,l) in meta){
                for(a in l){channelAccess.addPost(c,a)}
            }

        }

        Sync.queueUpdate()
    }

}