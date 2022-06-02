package daemon.dev.field.data

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import daemon.dev.field.MANAGER_TAG
import daemon.dev.field.data.db.ItemDatabase
import daemon.dev.field.data.db.ItemDatabaseDao
import daemon.dev.field.data.db.op.Clear
import daemon.dev.field.data.db.op.Query
import daemon.dev.field.data.db.op.Request
import daemon.dev.field.data.db.op.Update
import daemon.dev.field.data.objects.*
import daemon.dev.field.nypt.AES_KEY_SIZE
import daemon.dev.field.nypt.Key
import daemon.dev.field.nypt.USER_KEY_SIZE
import daemon.dev.field.util.AppDataIO
import daemon.dev.field.util.Serializer
import java.io.*
import kotlin.random.Random
import kotlin.random.nextULong


@RequiresApi(Build.VERSION_CODES.O)
object PostRAM {

    /**important user data*/
    lateinit var me : UserProfile
    var postCount : Int = 0

    /**Do I want to forward other peoples posts?*/
    var forwarding : Boolean = false
    var accepting : Boolean = false
    var mesh : Boolean = false

    /**My Channel Info*/
    private var _channelList : HashMap<String,Channel> = hashMapOf()
    val channelList : MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())
    val postList : MutableLiveData<MutableList<Post>> = MutableLiveData(mutableListOf())
    val binSel : MutableList<String> = mutableListOf()


    /**Live chat variables*/
    val newChat : MutableLiveData<ULong> = MutableLiveData()


    /**DB access objects*/
    private lateinit var fileio : AppDataIO
    lateinit var postDao : ItemDatabaseDao
    private lateinit var db: ItemDatabase

    /**Function to initialize and get references to local storage*/
    fun static_init(context : Context){

        /**Init db*/
        db = ItemDatabase.getInstance(context)
        postDao = db.itemDatabaseDao

        //check if my files exist
        val binData = File(context.filesDir, "bin.data")

        fileio = AppDataIO(binData)
        binData.delete()

        /**We need to add file space for post count and the user personal key*/

        if(binData.createNewFile()){ //doesn't exist
            Clear().start()

            /**Create users unique cryptographic key  ¯\_(ツ)_/¯*/
            val key = Key(USER_KEY_SIZE)
            val alias = "anon#" + Random.nextInt(9999).toString()
            me = UserProfile(alias,key)
            fileio.writeOfficialAppUserToSecureFile(me)

            _channelList["all"] = Channel("all")
            _channelList["yours"] = Channel("yours")
            _channelList["share"] = Channel("share")

            fileio.createChannels(_channelList)

        }else{ //does exist
            //load data from files
            me = fileio.getOfficialAppUserStoredOnThisDevice()

            _channelList = fileio.getChannels()

        }
        val keys = _channelList.keys.toList() as MutableList<String>
        channelList.postValue(keys)

        selectChannel("all")
    }

    fun getPost(uid : ULong) : Post?{
        for(p in postList.value!!){
            if(p.uid == uid){return p}
        }
        return null
    }

    fun deletePost(uid : ULong){

        for (b in _channelList.keys){
            if(_channelList[b]?.postList!!.contains(uid)){
                fileio.removeFromFile(_channelList[b]!!.name,uid)
                _channelList[b]?.postList!!.remove(uid)
            }
        }
        postDao.del(uid.toLong())

    }

    fun createBin(name:String){

        _channelList[name] = Channel(name)
        val keys = _channelList.keys.toList() as MutableList<String>
        channelList.postValue(keys)
        fileio.addBin(name)

    }

    fun static_close(){
        //close open files
        db.close()
    }

    fun setAlias(alias : String){
        me.alias = alias
    }

    fun getAlias() : String {
        return me.alias
    }

    fun selectChannel(bin_id : String) : Boolean {
        var ret = true
        val idList = mutableListOf<ULong>()

        if(binSel.contains(bin_id)){
            binSel.remove(bin_id)
            ret = false
        } else {
            binSel.add(bin_id)
        }

        Log.i(MANAGER_TAG, "Selected bins: $binSel")
        for(b in binSel){
            _channelList[b]?.postList?.let {
                for (p in it){
                    if(!idList.contains(p)){
                        idList.add(p)
                    }
                }
            }
        }

        Query(postList,idList).start()
        return ret
    }

    fun updatePost(){
        Log.i(MANAGER_TAG, "Selected bins: $binSel")
        val idList = mutableListOf<ULong>()
        for(b in binSel){
            _channelList[b]?.postList?.let {
                for (p in it){
                    if(!idList.contains(p)){
                        idList.add(p)
                    }
                }
            }
        }

        Query(postList,idList).start()
    }

    fun createMyPost(
        title: String,
        body: String,
        targets: List<String>,
        uri: String?,
    ) : ULong{
        val range = IntRange(0,3)

        val mid : ULong = Random.nextULong(9999999u)
//            bytesToLong(me.uid.get().sliceArray(range)
//                    +longToBytes(postCount++.toLong()).sliceArray(range))
//                .toULong()

        val time = System.currentTimeMillis()

        val entity = if(uri == null) {
            Message(title, body, time, time, mid, me)
        } else {
            Media(title, body, time, time, mid, me, Uri.parse(uri))
        }

        _channelList["all"]!!.putPost(mid)

        targets.forEach {
            Log.i(MANAGER_TAG,"$it")
            _channelList[it]?.putPost(mid)
        }

        Log.i(MANAGER_TAG,"Created post : $mid")

        Request(listOf(entity)).start()

        return mid
    }

    fun comment(post : Post, cList : MutableList<Comment>, text : String, user : UserProfile) : Comment{
        val time = System.currentTimeMillis()

        val comment = Comment(user,text,time,cList.size)
        cList.add(comment)

        val cString = Serializer().commentListToString(post.comments)

        Update(cString,post.uid.toLong()).start()

        return comment
    }

    fun buildDeviceInfo() : NodeInfo {
        return NodeInfo(me, forwarding, accepting,null,channelList.value)
    }

    fun storeNetworkPost(post : Post){

        val targets = post.targets
        post.hops +=1

        val all = _channelList["all"]!!

        if(!all.postList.contains(post.uid)){
            all.putPost(post.uid)
            targets?.forEach {
                _channelList[it]?.putPost(post.uid)
            }
        }

        val list = postList.value
        list?.let{
            for (p in list){
                if (p.uid == post.uid){
                    list.remove(p)
                    list.add(post)
                }
            }
        }

        Request(listOf(post)).start()
    }

    fun binListAsArray() : Array<String>{

        channelList.value!!.let{
            val len = it.size

            val array = arrayOfNulls<String>(len)

            var i = 0
            it.forEach { bin ->
                array[i] = bin
                i++
            }

            return array as Array<String>
        }

    }

    fun getBin(name : String) : Channel? {
        return _channelList[name]
    }

    class Channel(val name : String){
        val postList : MutableList<ULong> = mutableListOf()
        val key : Key = Key(AES_KEY_SIZE)

        fun putPost(uid : ULong){

            var here = false

            for (uLong in postList) {
                if(uLong == uid){
                    here = true
                }
            }
            if(!here){
                postList.add(uid)
            }

            fileio.addToFile(name,uid)
        }

    }
    fun getFromDB(uid : Long) : Post?{
        val entity = postDao.get(uid)
        return entity?.let {

            val comments = Serializer().commentListFromString(entity.cString)

            val post = if(it.uri == "null") {
                Message(it.title,
                    it.body,
                    it.time_created,
                    it.last_touched,
                    it.mid.toULong(),
                    Serializer().profileFromString(it.user)!!
                )
            } else {
                Media(it.title,
                    it.body,
                    it.time_created,
                    it.last_touched,
                    it.mid.toULong(),
                    Serializer().profileFromString(it.user)!!,
                    Uri.parse(it.uri)
                )
            }
            post.hops = it.hops
            post.comments = comments as MutableList<Comment>

            post
        }
    }
}
