package daemon.dev.field.network.handler.packet

import android.util.Log
import daemon.dev.field.CHARSET
import daemon.dev.field.HEX
import daemon.dev.field.cereal.objects.Address
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.Post
import daemon.dev.field.data.ChannelAccess
import daemon.dev.field.data.PostRepository
import daemon.dev.field.network.Socket
import daemon.dev.field.network.Sync
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.zip.CRC32

class NEW_DATAHandler(private val postRepository: PostRepository, val channelAccess: ChannelAccess) {

    private val crc = CRC32()

    suspend fun handle( raw : MeshRaw, socket : Socket) {
    Log.w("NEW_DATA.kt", "Received NEW_DATA")

        raw.newData?.let {
//                    Log.i("op.kt", "Got NEW_DATA adding posts to channel and comparing")
            val meta = hashMapOf<String,MutableList<String>>()
            val postList = mutableListOf<Post>()
            val contents = mutableListOf<String>()

            for ((c, posts) in it) {

                meta[c] = mutableListOf()

                for (i in channelAccess.waitContents(c).split(",")){
                    if(i == "null") continue

                    if(!contents.contains(i)){contents.add(i)}
                    meta[c]!!.add(i)

                }

                if(meta[c]!!.isEmpty()){ meta.remove(c) }

                for ((address, hash) in posts) {

                    contents.remove(address)
                    val post = postRepository.getAt(Address(address))
                    val content = post?.contentString()

                    if(content != null){
                        crc.reset()
                        crc.update(content.toByteArray(CHARSET))
                        val nHash = crc.value.toString(HEX)

                        Log.w("NEW_DATA.kt", "Comparing $address {me: $content : $nHash} v {peer: $hash}")

                        if(nHash != hash){
                            if(!postList.contains(post)){postList.add(post)}
                        }
                    }

                }

            }

            val json = Json.encodeToString(meta)

            for(p in contents){
                Log.w("NEW_DATA.kt", "adding $p to send")
                val post = postRepository.getAt(Address(p))
                if(!postList.contains(post) && post != null){
                        postList.add(post)
                }
            }

            if(postList.isNotEmpty()){
                Log.w("NEW_DATA.kt", "sending postList $postList")
                Log.w("NEW_DATA.kt", "sending meta $json")

                val newRaw = MeshRaw(
                    MeshRaw.POST_LIST,
                    null,
                    null,
                    null,
                    postList,
                    json
                )

                Sync.queue(socket.key, newRaw)
            }


        }

    }
}