package daemon.dev.field.data

import android.util.Log
import androidx.lifecycle.LiveData
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.UNIVERSAL_KEY
import daemon.dev.field.cereal.objects.Comment
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.Post
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.data.db.ChannelDao
import daemon.dev.field.data.db.PostDao
import daemon.dev.field.data.db.ResDao
import daemon.dev.field.data.db.UserDao
import daemon.dev.field.getTime
import daemon.dev.field.nypt.ChannelBuilder
import daemon.dev.field.toBase64
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

class SyncInterface(
    postDao: PostDao,
    channelDao: ChannelDao,
    userDao: UserDao) {

    val ca = ChannelAccess(channelDao)
    val pr = PostRepository(postDao)
    val ub = UserBase(userDao)


    val channels = ca.channels
    val me = ub.getUser(PUBLIC_KEY.toBase64())

    fun users() : List<User>{
        return ub.users()
    }
    suspend fun clearDB(){
        ub.clear()
        pr.clear()
        ca.clear()
        ca.createChannel("Public", UNIVERSAL_KEY)
        ub.add(User(PUBLIC_KEY.toBase64(),"anon#${Random.nextInt(999)}",0, "null"))

    }

    suspend fun setAlias(alias : String){
            ub.setAlias(alias)
    }

    fun getUser(key : String) : LiveData<User> {
        return ub.getUser(key)
    }

    suspend fun setUserStatus(key : String, status : Int){
        ub.setUserStatus(key,status)
    }

    fun get(position : Int) : LiveData<Post> {
        return pr.get(position)
    }

    suspend fun removeChannel(name : String){
            ca.delete(name)
    }

    suspend fun buildChannel(name : String) {
        val builder = ChannelBuilder(name)
        ca.updateKey(name,builder.key().toBase64())
    }

    suspend fun addChannel(name : String){
        val builder = ChannelBuilder(name)
        ca.createChannel(name,builder.key().toBase64())
    }

    suspend fun map(open: List<String>): HashMap<String, MutableList<String>> {
        return pr.mapOpenChannels(open)
    }

    fun getListPostFromChannelQuery(open : List<String>) : LiveData<List<Post>>{
        return pr.getListPostFromChannelQuery(open)
    }

    suspend fun create(
        title: String,
        body: String,
        open: List<String>,
    ): HashMap<String, MutableList<String>>? {
        val time = System.currentTimeMillis()

        val post = Post(PUBLIC_KEY.toBase64(), time, title, body, "null", 0)

        if (open.isEmpty()) {
            Log.e("SyncModel.kt", "Err no open channels, post not created")
            return null
        }

        pr.add(post)

        for (c in open) {
            pr.addPostToChannel(c, post.address())
        }

        return pr.mapOpenChannels(open)
    }

    suspend fun comment(position : Int, sub : MutableList<Comment>, globalSub : MutableList<Comment>, text : String) : Comment {
        Log.v("SyncModel.kt", "Creating comment $text")

        val comment = Comment(PUBLIC_KEY.toBase64(), text, getTime())
        sub.add(comment)
        val post = pr.getByIndex(position)
        post.comment = Json.encodeToString(globalSub)
        pr.stage(listOf(post))
        pr.commit()

        return comment
    }
    suspend fun handleInfo(user : User, open : List<String>) : MeshRaw? {
        val channels = Json.decodeFromString<HashMap<String,String>>(user.channels)
        user.channels = "null"

        val _user = ub.wait(user.key)
        Log.i("INFO.kt", "Got user ${_user?.print()}")

        if(_user != null){
            user.status = _user.status
        }

        val dif = mutableListOf<String>()

        Log.i("INFO.kt", "Receive peer channels $channels")
        val channelInfo = pr.hashSelectChannels(channels.keys.toTypedArray().toList())
        for ((c, h) in channels) {
            if (channelInfo[c] != h && c in open) {
                dif.add(c)
            }
        }
        Log.i("INFO.kt", "Have dif $dif")
        return if (dif.isNotEmpty()) {
            val data_map = pr.createDataMap(dif)

            Log.i(
                    "INFO.kt",
                    "Have channel_to_list_of_post_address $data_map"
            )

            if (data_map.isNotEmpty()) {
                val newRaw = MeshRaw(MeshRaw.NEW_DATA, null, null, data_map, null, null)
                newRaw
            } else { null }
        } else { null }

    }

    suspend fun buildInfo(open: List<String>): MeshRaw {

        val info = ub.wait(PUBLIC_KEY.toBase64())!!
        val channelInfo = pr.hashSelectChannels(open)
        info.channels = Json.encodeToString(channelInfo)

        return MeshRaw(MeshRaw.INFO, info, null, null, null, null)

    }


}