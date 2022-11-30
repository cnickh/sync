package daemon.dev.field.network.handler

import android.util.Log
import daemon.dev.field.*
import daemon.dev.field.cereal.objects.HandShake
import daemon.dev.field.network.Async
import daemon.dev.field.network.Socket
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ResolverEventHandler {

    suspend fun handleResolverEvent(event: ResolverEvent) : Int{

        var ret = 1

        when(event.type){
            RETRY ->{
                ret = -1
            }
            DISCONNECT ->{
                ret = 0
            }
            PACKET ->{
                Async.receive(event.bytes!!,event.socket!!)
            }
            HANDSHAKE ->{
                if(event.bytes == null){

                    val shake = Async.handshake()

                    if(shake.state == Async.READY){
                        val service = event.gatt!!.getService(SERVICE_UUID)
                        val char = service.getCharacteristic(REQUEST_UUID)
                        val json = Json.encodeToString(shake)
                        char.value = json.toByteArray(CHARSET)
                        event.gatt.writeCharacteristic(char)
                    }

                }else{
                    val gatt = event.gatt!!
                    val service = gatt.getService(SERVICE_UUID)
                    val char = service.getCharacteristic(PROFILE_UUID)
                    gatt.setCharacteristicNotification (char, true)

                    val json = event.bytes.toString(CHARSET)
                    val shake = try{
                        Json.decodeFromString<HandShake>(json)
                    }catch(e : Exception){
                        Log.e(NETLOOPER_TAG,"handleResolverEvent: Error decoding handshake")
                        Log.e(NETLOOPER_TAG, json)
                        ret = 0
                        null
                    }

                    shake?.let{

                        val sock = Socket(it.me, Socket.BLUETOOTH_GATT, null, gatt, event.device!!,null)
                        event.res!!.socket = sock

                        if(!Async.connect(sock,it.me)){
                            Log.e(NETLOOPER_TAG,"handleResolverEvent: Too many peers canceling connect")
                            ret = 0
                        }

                    }
                }//if(event.bytes == null)

            }//HANDSHAKE

        }//when(event.type)
        if(ret == 0){stopResolverConnection(event)}
        return ret
    }//handleResolverEvent

    private suspend fun stopResolverConnection(event: ResolverEvent){
        Log.w(NETLOOPER_TAG,"handleGattEvent: Got disconnect event")
        if(event.socket == null){
            event.gatt?.disconnect()
        }else{
            Async.disconnectSocket(event.socket)
        }
    }

}