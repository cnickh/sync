package daemon.dev.field.network

import daemon.dev.field.SYNC_INTERVAL
import daemon.dev.field.cereal.objects.MeshRaw
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex

object Sync {

    private lateinit var sync : SyncThread
    val open_channels = mutableListOf<String>()
    val peer_queue = hashMapOf<String,ArrayDeque<MeshRaw>>()
    val queue_lock = Mutex()

    fun start(){
        sync = SyncThread()
        sync.start()
    }

    suspend fun kill(){
        sync.kill()
    }

    suspend fun add(peer : String){
        //queue_lock.lock()
        peer_queue[peer] = ArrayDeque()
       // queue_lock.unlock()
    }

    suspend fun remove(peer : String){
       // queue_lock.lock()
        peer_queue.remove(peer)
       // queue_lock.unlock()
    }

    suspend fun queue(peer : String, raw : MeshRaw){
       // queue_lock.lock()
        peer_queue[peer]?.addLast(raw)
      //  queue_lock.unlock()
    }

    fun selectChannel(name : String) : Boolean{
        return if(open_channels.contains(name)){
            open_channels.remove(name)
            false
        }else{
            open_channels.add(name)
            true
        }
    }

    class SyncThread : Thread() {

        private var active = true
        var i = 0

        override fun run() {
            runBlocking { while(active){

                val info = Async.me()
                info.channels = open_channels.joinToString(",")
                val raw = MeshRaw(MeshRaw.INFO, info, null, null, null, null)

              //  queue_lock.lock()

                if (peer_queue.toList().isNotEmpty()){
                    val p = i++ % peer_queue.size
                    val (key,queue) = peer_queue.toList()[p]

                    if(Async.checkKey(key)){

                        if(queue.isEmpty()){
                            Async.send(raw, key)
                        }else{
                            Async.send(queue.removeFirst(), key)
                        }

                    }else{
                        peer_queue.remove(key)
                    }

                }

              //  queue_lock.unlock()
                sleep(SYNC_INTERVAL)
            } }

        }

        suspend fun kill(){
            active = false
           // queue_lock.lock()
            peer_queue.clear()
         //   queue_lock.unlock()
        }
    }

}