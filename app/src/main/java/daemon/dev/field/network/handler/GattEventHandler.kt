package daemon.dev.field.network.handler

import android.util.Log
import daemon.dev.field.CHARSET
import daemon.dev.field.NETLOOPER_TAG
import daemon.dev.field.cereal.objects.HandShake
import daemon.dev.field.network.Async
import daemon.dev.field.network.NetworkLooper
import daemon.dev.field.network.Socket
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GattEventHandler {

    suspend fun handleGattEvent(event : GattEvent){

        when(event.type){

            DISCONNECT ->{
                stopGattConnection(event)
            }
            PACKET ->{
                Log.i(NETLOOPER_TAG,"handleGattEvent: got PACKET")
                val socket = Async.getSocket(event.device)

                if(socket == null){
                    Log.i(NETLOOPER_TAG,"handleGattEvent: reading handshake")

                    val json = event.bytes!!.toString(CHARSET)
                    val shake = try{
                        Json.decodeFromString<HandShake>(json)
                    }catch(e : Exception){
                        Log.e(NETLOOPER_TAG,"handleGattEvent: Error decoding handshake")
                        Log.e(NETLOOPER_TAG, json)
                        stopGattConnection(event)
                        null
                    }

                    shake?.let {
                        event.gattServer?.sendResponse(
                            event.device, event.req!!, 200, 0, null)

                        val sock = Socket(it.me, Socket.BLUETOOTH_DEVICE, null, null, event.device, event.gattServer)

                        if(!Async.connect(sock,it.me)){
                            Log.e(NETLOOPER_TAG,"handleGattEvent: Too many peers canceling connect")
                            stopGattConnection(event)
                        }

                    }

                }else{
                    event.gattServer?.sendResponse(
                        event.device, event.req!!, 200, 0, null)
                    event.bytes?.let { Async.receive(it, socket) }
                }
            } //PACKET
            HANDSHAKE ->{
                val shake = Async.handshake()
                val json = Json.encodeToString(shake)
                if(shake.state == Async.READY){
                    Log.i(NETLOOPER_TAG,"Sending response $json")

                    event.gattServer?.sendResponse(
                        event.device, event.req!!, 0, 0, json.toByteArray(CHARSET))
                } else {
                    stopGattConnection(event)
                }
            }

        }//when(event.type)

    }//handlerGattEvent

    private suspend fun stopGattConnection(event: GattEvent){
        Log.w(NETLOOPER_TAG,"handleGattEvent: Got disconnect event")

        event.req?.let {
            event.gattServer?.sendResponse(
                event.device, it, 600, 0, null)
        }

        val sock = Async.getSocket(event.device)
        sock?.let{
            Async.disconnectSocket(it)
        }
//        event.gattServer?.cancelConnection(event.device)
    }

}