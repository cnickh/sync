package daemon.dev.field.data

import android.util.Log
import daemon.dev.field.cereal.objects.Address
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.Post
import daemon.dev.field.data.db.PostDao
import daemon.dev.field.network.Async
import daemon.dev.field.util.CommentMerge
/**@brief this class wraps around database accesses and updates. It ensures there is not duplicate
 * data and can merge post threads together via the CommentMerge class. It also provides live data
 * objects to the UI ViewModel. */

class PostRepository(private val sync : PostDao) {

    //Make all posts available
    val posts = sync.getPosts()

    //Live update comment from the network
    //val comment =

    private val addressCache = HashMap<Address,Post>()
    private val staged = mutableListOf<Post>()


    suspend fun add(post : Post){
        val cur = getAt(post.address())

        if(cur != null){
            stage(listOf(post))
            commit()
            return
        }

        sync.insert(post)

        val data = hashMapOf<String,String>()
        val address = post.address().address
        data[address] = post.hash()
//        val raw = MeshRaw(
//            MeshRaw.NEW_DATA,
//            null,
//            null,
//            data,
//            null,
//            null
//        )
//        Async.sendAll(raw)

        Log.d("PostRepository.kt","Inserted address: ${post.address()}")
    }

    suspend fun getAt(address : Address) : Post? {
        return sync.getAt(address.key(), address.time())
    }

    suspend fun get(key : String) : Int {
        return sync.getKey(key)
    }

    suspend fun hashList(posts : List<String>) : HashMap<String,String>{
        val map = HashMap<String,String>()

        for(a in posts){
            val post = getAt(Address(a))
            if (post != null) {
                map[post.address().address] = post.hash()
            }
        }

        return map
    }

    suspend fun getList(posts : List<String>) : List<Post>{

        val list = mutableListOf<Post>()

        for(a in posts){
            val address = Address(a)
            getAt(address)?.let { list.add(it) }
        }
        Log.d("PostRepository.kt","Got list $list")

        return list
    }

    suspend fun compare(posts : HashMap<String,String>) : List<String> {

        val requests = mutableListOf<String>()

        for(a in posts.keys){
            val address = Address(a)
            val hash = getAt(address)?.hash()
            if(hash == null || hash != posts[a]){
                requests.add(address.address)
            }
        }

        return requests
    }

    suspend fun stage(posts : List<Post>) {
        Log.d("PostRepository.kt","Staging")

        for(p in posts){
            Log.d("PostRepository.kt","$p")

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
            Log.d("PostRepository.kt","Updating with ${p}")
            sync.updateComment(p.comment, p.index)
            post = p
        }
        Log.i("PostRepository.kt","got post: ${getAt(post!!.address())}")
//        Log.i("PostRepository.kt","Got ${sync.get(id).comment}")
        staged.clear()
    }

}
