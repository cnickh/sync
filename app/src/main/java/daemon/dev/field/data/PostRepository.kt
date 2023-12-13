package daemon.dev.field.data

import android.util.Log
import androidx.lifecycle.LiveData
import daemon.dev.field.CHARSET
import daemon.dev.field.HEX
import daemon.dev.field.cereal.objects.Address
import daemon.dev.field.cereal.objects.IndexEntity
import daemon.dev.field.cereal.objects.Post
import daemon.dev.field.data.db.PostDao
import daemon.dev.field.util.CommentMerge
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.zip.CRC32

/**@brief this class wraps around database accesses and updates. It ensures there is not duplicate
 * data and can merge post threads together via the CommentMerge class. It also provides live data
 * objects to the UI ViewModel. */

class PostRepository(private val sync : PostDao) {

    //Make all posts available
    private val crc = CRC32()
    private val staged = mutableListOf<Post>()

    fun getListPostFromChannelQuery(list : List<String>) : LiveData<List<Post>>{
        return sync.getListPostFromChannelQuery(list)
    }

    suspend fun createDataMap(list : List<String>) : HashMap<String, HashMap<String, String>>{
        val ret = hashMapOf<String, HashMap<String, String>>()
        for (c in list){
            val indexes = sync.postsInChannel(c)
            ret[c] = hashList(indexes)
        }

        return ret
    }

    suspend fun hashSelectChannels(list : List<String>) : HashMap<String,String> {
        val ret = hashMapOf<String,String>()
        for (c in list){
            val indexes = sync.postsInChannel(c)
            ret[c] = hashListCombined(indexes)
        }

        return ret
    }

    suspend fun addressesInChannel(channel : String) : List<String> {
        val ret = mutableListOf<String>()
        val indexes = sync.postsInChannel(channel)
        for (i in indexes){
            ret.add(sync.get(i).address().address)
        }

        return ret
    }

    suspend fun addPostToChannel(channel:String, address: Address) : Boolean{
        val post = sync.getAt(address.key(), address.time())
        return if(post == null){
            false
        } else {
            val index = post.index
            sync.addIndex(IndexEntity(channel,index))
            true
        }
    }

    fun clear(){
        sync.clear()
        sync.clearIndexes()
    }

    suspend fun add(post : Post){
        val cur = getAt(post.address())

        if(cur != null){
            stage(listOf(post))
            commit()
            return
        }

        sync.insert(post)

        Log.v("PostRepository.kt","Inserted address: ${post.address()}")
    }

    suspend fun getAt(address : Address) : Post? {
        return sync.getAt(address.key(), address.time())
    }

    suspend fun get(key : String) : Int {
        return sync.getKey(key)
    }

    suspend fun getByIndex(index: Int) : Post {
        return sync.get(index)
    }
    fun get(index : Int) : LiveData<Post> {
        return sync.getLive(index)
    }


    suspend fun mapOpenChannels(channels: List<String>): HashMap<String,MutableList<String>>{
        val ch_map = hashMapOf<String,MutableList<String>>()

        for(c in channels){
            val content = sync.getPostsInChannels(c)

            ch_map[c] = mutableListOf()

            if(content.isNotEmpty()) {
                for (p in content) {
                    val address= sync.get(p).address()
                    ch_map[c]!!.add(address.address)
                }
            }

        }

        return ch_map
    }

    private suspend fun hashListCombined(posts: List<Int>): String {
        var content = ""
        for (a in posts) {
            val post = sync.get(a)
            content += post.contentString()
        }
        if(content == ""){return "null"}
        crc.update(content.toByteArray(CHARSET))
        val hash = crc.value.toString(HEX)

        crc.reset()

        return hash
    }

    suspend fun hashList(posts : List<Int>) : HashMap<String,String>{
        val map = HashMap<String,String>()

        for(a in posts){
            val post = sync.get(a)
            val json = Json.encodeToString(post)
            Log.i("PostRepository.kt","Got $json")

            if (post != null) {

                val content = post.contentString()
                crc.reset()
                crc.update(content.toByteArray(CHARSET))
                val hash = crc.value.toString(HEX)

                Log.i("PostRepository.kt"," --CRC-Hashing-- \n :: $content To ${crc.value} string:[$hash]")

                map[post.address().address] = hash

            }
        }

        return map
    }


    suspend fun stage(posts : List<Post>) {
        Log.v("PostRepository.kt","Staging")

        for(p in posts){
            Log.v("PostRepository.kt","$p")

            val origin = getAt(p.address())
            if(origin != null){
                val final = merge(p,origin)
                staged.add(final)
            }else{
                staged.add(p)
            }
        }
    }

    private fun merge(p0: Post, p1: Post): Post {
        val res = CommentMerge(p0.comment, p1.comment).getResult()
        val post = Post(p0.key, p0.time, p0.title, p0.body, res, p0.hops)
        post.index = p1.index
        return post
    }

    suspend fun commit(){
        var post : Post? = null
        for(p in staged){
            Log.v("PostRepository.kt","Updating with ${p}")
            sync.updateComment(p.comment, p.index)
            post = p
        }
        Log.v("PostRepository.kt","got post: ${getAt(post!!.address())}")
//        Log.i("PostRepository.kt","Got ${sync.get(id).comment}")
        staged.clear()
    }

}
