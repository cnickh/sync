package daemon.dev.field.network.util


import android.util.Log
import androidx.lifecycle.MutableLiveData
import daemon.dev.field.CONFIRMATION_TIMEOUT
import daemon.dev.field.network.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class Verifier(private val ping : MutableLiveData<String>) {

    private val pending_message = hashMapOf<Socket,MutableList<Triple<Int,Int,Long>>>() // tracks pending messages
    private val pending_socket = hashMapOf<Socket,Long>() //tracks socket responsiveness
    private val vr = Mutex()

    suspend fun clear(socket : Socket){
        Log.v("Verifier.kt","emptying pending messages")

        vr.lock()
        pending_socket.remove(socket)
        pending_message[socket]?.clear()
        vr.unlock()
    }

    suspend fun add(socket : Socket, msg : Triple<Int,Int,Long>) {
       Log.v("Verifier.kt","Getting vrLock add")

        vr.lock()

        if (pending_message[socket] == null){
            pending_message[socket] = mutableListOf(msg)
        }else{
            pending_message[socket]!!.add(msg)
        }
        Log.v("Verifier.kt","Dropping vrLock")
        vr.unlock()


        CoroutineScope(Dispatchers.IO).launch {
            delay(CONFIRMATION_TIMEOUT)
            checkConfirm(socket, msg.first)
        }

    }

    suspend fun confirm(socket : Socket, mid : Int){

        Log.v("Verifier.kt","Getting vrLock confirm")
        vr.lock()

        val cur = System.currentTimeMillis()
        var msg : Triple<Int,Int,Long>? = null
        pending_socket[socket] = cur

        if(pending_message[socket] == null){
            Log.e("Verifier.kt","[already removed] Message $mid")
            Log.v("Verifier.kt","Dropping vrLock")
            vr.unlock()
            return
        } else {
            for (m in pending_message[socket]!!){
                if (m.first == mid){
                    msg = m
                    break
                }
            }
        }

        if(msg!=null){
            pending_message[socket]!!.remove(msg)
            ping.postValue("${cur - msg.third} ${msg.second} ${msg.first}")
            Log.v("Verifier.kt","[was received :)] Message $mid")
        } else{
            Log.e("Verifier.kt","[already removed] Message $mid")
        }

        Log.v("Verifier.kt","Dropping vrLock")
        vr.unlock()

    }

    private suspend fun checkConfirm(socket : Socket, mid : Int){

        val cur = System.currentTimeMillis()
        Log.v("Verifier.kt","Getting vrLock checkConfirm")
        vr.lock()

        val dif = pending_message[socket]?.let {
            var msg: Triple<Int, Int, Long>? = null
            for (m in it) {
                if (m.first == mid) {
                    msg = m
                    break
                }
            }

            if (msg == null) {

                Log.v("Verifier.kt","$mid not found to be pending");
                Log.v("Verifier.kt","Dropping vrLock");
                vr.unlock();
                return
            } else {
                Log.e("Verifier.kt", "Message $mid not received")
                it.remove(msg)
            }

            pending_socket[socket]?.let {
                cur - it
            }

        }

        Log.v("Verifier.kt","Dropping vrLock")
        vr.unlock()

        if (dif == null) {
            Log.e("Verifier.kt","peer[${socket.key}|${socket.type2String()}] not responding dif == null")
            //Async.disconnectSocket(socket)
        }else{
            if(dif > CONFIRMATION_TIMEOUT){
                Log.e("Verifier.kt","peer[${socket.key}|${socket.type2String()}] not responding dif == $dif")
                //Async.disconnectSocket(socket)
            } else {
                Log.v("Verifier.kt","peer[${socket.key}|${socket.type2String()}] not responding dif == $dif")
            }
        }

    }

}
