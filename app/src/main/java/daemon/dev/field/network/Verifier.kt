package daemon.dev.field.network;

import android.os.Handler
import android.os.Looper
import android.util.Log
import daemon.dev.field.BLE_INTERVAL

class Verifier {

    val waiting = mutableListOf<String>()

    fun add(hash : String, hops : Int){

        waiting.add(hash)

        Handler(Looper.getMainLooper()).postDelayed({
            //post delayed check and throw error
            confirm(hash)
        }, BLE_INTERVAL*hops)

    }

    fun confirm(hash : String){

        if(waiting.contains(hash)){
            waiting.remove(hash)
        }else{
            Log.e("Async","Message $hash not received :(")
        }

    }


}
