package daemon.dev.field.network.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import daemon.dev.field.CONFIRMATION_TIMEOUT
import daemon.dev.field.network.Async
import daemon.dev.field.network.Socket
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex

class Verifier {

    private val pending_message = hashMapOf<Socket,MutableList<Int>>()
    private val pending_socket = hashMapOf<Socket,Long>()
    private val vr = Mutex()



    suspend fun clear(socket : Socket){
        Log.v("Verifier.kt","here0")

//        vr.lock()
        pending_socket.remove(socket)
        pending_message[socket]?.clear()
//        vr.unlock()
    }

    suspend fun add(socket : Socket, mid : Int){
       // Log.w("Verifier.kt","here1")

//        vr.lock()

        if (pending_message[socket] == null){
            pending_message[socket] = mutableListOf(mid)
        }else{
            pending_message[socket]!!.add(mid)
        }

//        vr.unlock()

        Handler(Looper.getMainLooper()).postDelayed({
            //post delayed check and throw error
            runBlocking {checkConfirm(socket, mid)}
        }, CONFIRMATION_TIMEOUT)

    }

    suspend fun confirm(socket : Socket, mid : Int){
       // Log.w("Verifier.kt","here2")

//        vr.lock()

        pending_socket[socket] = System.currentTimeMillis()

        if(pending_message[socket] == null){
            Log.e("Verifier.kt","[already removed] Message $mid")
//            vr.unlock()
            return
        }

        if(pending_message[socket]!!.contains(mid)){
            pending_message[socket]!!.remove(mid)
            Log.v("Verifier.kt","[was received :)] Message $mid")
        }else{
            Log.e("Verifier.kt","[already removed] Message $mid")
        }
//        vr.unlock()

    }

    private suspend fun checkConfirm(socket : Socket, mid : Int){
       // Log.w("Verifier.kt","here3")

//        vr.lock()

        if(!pending_message[socket]!!.contains(mid)){
            return
        } else {
            Log.e("Verifier.kt","Message $mid not received")
            pending_message[socket]!!.remove(mid)
        }

        val dif = pending_socket[socket]?.let{
            System.currentTimeMillis() - it
        }

//        vr.unlock()


        if (dif == null) {
            Log.e("Verifier.kt","peer[${socket.key}|${socket.type2String()}] not responding dif == null")
            Async.disconnectSocket(socket)
        }else{
            if(dif > CONFIRMATION_TIMEOUT){
                Log.e("Verifier.kt","peer[${socket.key}|${socket.type2String()}] not responding dif == $dif")
                Async.disconnectSocket(socket)
            }
        }
    }

}
