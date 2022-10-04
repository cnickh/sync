package daemon.dev.field.network

import daemon.dev.field.SYNC_INTERVAL
import daemon.dev.field.cereal.objects.MeshRaw
import kotlinx.coroutines.runBlocking

object Sync {

    val open_channels = mutableListOf<String>()

    fun init_connection(key : String){
        val sync = SyncThread(key)
        sync.start()
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

    class SyncThread(val key : String) : Thread() {

        var active = true

        override fun run() {
            while(active){

                runBlocking {

                    val info = Async.me()

                    info.channels = open_channels.joinToString(",")

                    val raw = MeshRaw(MeshRaw.INFO, info, null, null, null, null)

                    active = Async.checkKey(key)
                    if(active){
                        Async.send(raw, key)
                    }
                }

                sleep(SYNC_INTERVAL)
            }
        }
    }

}