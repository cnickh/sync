package daemon.dev.field.network.util

import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.network.Async
import kotlinx.coroutines.runBlocking

class LoadBox(val key : String, val dataSize : Int)  : Thread() {

    private var running = true

    override fun run(){

        while(running){


            var payload = ""

            for (i in 0..dataSize){
                payload += "#"
            }

            val raw = MeshRaw(MeshRaw.PING,
                null,
                null,
                null,
                null,
                payload)

            runBlocking {
                Async.send(raw, key)
            }
        }

    }

    fun kill(){
        running = false
    }

}