package daemon.dev.field.network

import android.util.Log
import androidx.lifecycle.MutableLiveData
import daemon.dev.field.SYNC_INTERVAL
import daemon.dev.field.SYNC_TAG
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.data.ChannelAccess
import daemon.dev.field.data.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * This object is meant to keep track of a queue, consisting of post in open/active
 * channels and notify connected peers of the open channels on this instance of the
 * app
 */

object Sync {

    private lateinit var sync : SyncThread
    private lateinit var ca : ChannelAccess
    private lateinit var pr : PostRepository

    val open_channels = mutableListOf<String>()
    val liveChannels = MutableLiveData<List<String>>(listOf())


    val peer_queue = hashMapOf<String,ArrayDeque<MeshRaw>>()
    val queue_lock = Mutex()
    val openChannel_lock = Mutex()


    lateinit var channel_info : HashMap<String,String>
    private var raw : MeshRaw? = null

    private var running : Boolean = false

    fun start(ca : ChannelAccess, pr : PostRepository){
        this.ca = ca
        this.pr = pr
        sync = SyncThread()
        sync.start()
        running = true
        channel_info = hashMapOf()
    }

    suspend fun kill(){
        sync.kill()
        running = false
    }

    suspend fun add(peer : String){
        queue_lock.lock()
        peer_queue[peer] = ArrayDeque()
        queue_lock.unlock()
    }

    suspend fun remove(peer : String){
        queue_lock.lock()
        peer_queue.remove(peer)
        queue_lock.unlock()
    }

    suspend fun queue(peer : String, raw : MeshRaw){
        queue_lock.lock()
        peer_queue[peer]?.addFirst(raw)
        queue_lock.unlock()
    }

    suspend fun channelInfo(c : String) : String?{
        queue_lock.lock()
        Log.i("INFO.kt(Sync.kt)", "comparing $c to ${channel_info[c]}")
        val ret = channel_info[c]
        queue_lock.unlock()
        return ret
    }

    suspend fun queueUpdate(){
        if(!running){return}
        queue_lock.lock()

        val info = Async.me()
        val hmap = hashMapOf<String,String>()
        val map = ca.mapOpenChannels()

        if(map.isEmpty()){
            queue_lock.unlock()
            return
        }

        for ((c,l) in map){
            val hash = pr.hashListCombined(l)
            if(hash != "null"){
                hmap[c] = pr.hashListCombined(l)
            }
        }

        if(hmap.isEmpty()){
            queue_lock.unlock()
            return
        }

        Log.v(SYNC_TAG,"Updating raw with $hmap")
        channel_info = hmap
        info.channels = Json.encodeToString(hmap)

        raw = MeshRaw(MeshRaw.INFO, info, null, null, null, null)

        for ((p,q) in peer_queue) {

            if (q.isNotEmpty() && q.first().type == MeshRaw.INFO) {
                q.removeFirst()
            }

            q.addFirst(raw!!)

        }
        queue_lock.unlock()
    }

    suspend fun selectChannel(name : String){

        openChannel_lock.lock()

        if(open_channels.contains(name)){
            open_channels.remove(name)
        }else{
            open_channels.add(name)
        }

        liveChannels.postValue(open_channels)

        openChannel_lock.unlock()

    }

    suspend fun getOpenChannels() : List<String> {

        openChannel_lock.lock()
        val list = open_channels
        openChannel_lock.unlock()

        return list
    }

    class SyncThread : Thread() {

        private var active = true
        var i = 0

        override fun run() {
            runBlocking { while(active){

                queue_lock.lock()

                if (peer_queue.toList().isNotEmpty()){
                    val p = i++ % peer_queue.size
                    val (key,queue) = peer_queue.toList()[p]

                    if(Async.checkKey(key)){
                        if(queue.isNotEmpty())
                            Async.send(queue.removeFirst(), key)
                    }else{
                        peer_queue.remove(key)
                    }

                }

                queue_lock.unlock()
                withContext(Dispatchers.IO) { sleep(SYNC_INTERVAL) }
            } }
        }

        suspend fun kill(){
            active = false
            queue_lock.lock()
            peer_queue.clear()
            queue_lock.unlock()
        }
    }

}