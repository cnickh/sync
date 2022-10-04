package daemon.dev.field.network.util;

import android.os.Handler
import android.os.Looper
import android.util.Log
import daemon.dev.field.BLE_INTERVAL
import daemon.dev.field.network.Async
import daemon.dev.field.network.Socket
import kotlinx.coroutines.runBlocking

class Verifier {

    private val waiting = mutableListOf<String>()

    fun add(socket : Socket, hash : String, hops : Int){

        waiting.add(hash)

        Handler(Looper.getMainLooper()).postDelayed({
            //post delayed check and throw error
            runBlocking {checkConfirm(socket, hash)}
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

    private suspend fun checkConfirm(socket : Socket, hash : String){

        if(waiting.contains(hash)){
            Log.e("V","Message $hash not received :(")
            waiting.remove(hash)
            Async.disconnectSocket(socket)
        }

    }

}
