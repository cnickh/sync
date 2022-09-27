package daemon.dev.field.network;

import android.os.Handler
import android.os.Looper
import android.util.Log
import daemon.dev.field.BLE_INTERVAL

class Verifier {

    private val waiting = mutableListOf<String>()

    fun add(hash : String, hops : Int){

        waiting.add(hash)

        Handler(Looper.getMainLooper()).postDelayed({
            //post delayed check and throw error
            checkConfirm(hash)
        }, BLE_INTERVAL*hops)

    }

    fun confirm(hash : String){

        if(waiting.contains(hash)){
            waiting.remove(hash)
            Log.w("V","Message $hash was received :)")

        }else{
            Log.e("V","Message $hash already removed")
        }

    }

    private fun checkConfirm(hash : String){

        if(waiting.contains(hash)){
            Log.e("V","Message $hash not received :(")
            waiting.remove(hash)
        }

    }

}
