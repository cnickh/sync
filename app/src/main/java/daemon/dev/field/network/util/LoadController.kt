package daemon.dev.field.network.util

import android.util.Log
import daemon.dev.field.PEER_NET
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.util.ServiceLauncher
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class LoadController(val meshServiceController : ServiceLauncher)  : Thread() {

    val loadControllers = mutableListOf<LoadRunner>() //for test purposes

    fun init_loadBox(key : String){
        val box = LoadRunner(key,1000)
        loadControllers.add(box)
        box.start()
    }

    fun killLoad(){
        for (b in loadControllers){
            b.kill()
        }
        loadControllers.clear()
    }

    fun load(key : String) : Boolean {
        for (b in loadControllers){
            if(b.key.equals(key)){
                return true
            }
        }
        return false
    }

    inner class LoadRunner(val key : String, val dataSize : Int) : Thread(){

        private var running = true

        override fun run(){

            Log.i("LoadRunner","run() called $running $key")


            runBlocking {

                while(running){

//                if(!Async.canSend(key)){
//                    Log.v("LoadController.kt", "No Gatt exiting")
//                    running=false
//                }

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

                    Log.i("LoadRunner","queuing w/ meshServiceController")
                    meshServiceController.send(key, raw)
                    delay(2000) //2 seconds
                }
            }

        }

        fun kill(){
            running = false
        }

    }


}