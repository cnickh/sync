package daemon.dev.field.network.util;

import android.os.Handler
import android.os.Looper
import android.util.Log
import daemon.dev.field.CONFIRMATION_TIMEOUT
import daemon.dev.field.network.Async
import daemon.dev.field.network.Socket
import kotlinx.coroutines.runBlocking

class Verifier {

    private val pending_message = mutableListOf<Int>()
    private val pending_socket = hashMapOf<Socket,Long>()

    fun add(socket : Socket, mid : Int){

        pending_message.add(mid)

        Handler(Looper.getMainLooper()).postDelayed({
            //post delayed check and throw error
            runBlocking {checkConfirm(socket, mid)}
        }, CONFIRMATION_TIMEOUT)

    }

    fun confirm(socket : Socket, mid : Int){

        pending_socket[socket] = System.currentTimeMillis()

        if(pending_message.contains(mid)){
            pending_message.remove(mid)
            Log.w("Verifier.kt","[was received :)] Message $mid")
        }else{
            Log.e("Verifier.kt","[already removed] Message $mid")
        }

    }

    private suspend fun checkConfirm(socket : Socket, mid : Int){

        val dif = pending_socket[socket]?.minus(System.currentTimeMillis())

        if (dif == null) {
            Log.e("Verifier.kt","peer not responding dif == null")
            Async.disconnectSocket(socket)
        }else{
            if(dif > CONFIRMATION_TIMEOUT){
                Log.e("Verifier.kt","peer not responding dif == $dif")
                Async.disconnectSocket(socket)
            }
        }

        if(pending_message.contains(mid)){
            Log.e("Verifier.kt","[not received :(] Message $mid")
            pending_message.remove(mid)
        }

    }

}
