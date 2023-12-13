package daemon.dev.field.network.handler

import android.util.Log
import daemon.dev.field.cereal.objects.Address
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.data.PostRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.collections.HashMap

class POST_LISTHandler(val postRepository: PostRepository) {

    fun handle(raw: MeshRaw){
        CoroutineScope(Dispatchers.IO).launch{

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
                    for(a in l){postRepository.addPostToChannel(c, Address(a))}
                }

            }

        }
        //Sync.queueUpdate()
    }
}