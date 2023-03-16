package daemon.dev.field.network.handler.event

import android.annotation.SuppressLint
import android.util.Log
import daemon.dev.field.*
import daemon.dev.field.cereal.objects.HandShake
import daemon.dev.field.cereal.objects.KeyBundle
import daemon.dev.field.network.Async
import daemon.dev.field.network.Socket
import daemon.dev.field.network.handler.*
import daemon.dev.field.nypt.Session
import daemon.dev.field.nypt.Signature
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import java.util.*

class ResolverEventHandler {

    @SuppressLint("MissingPermission")
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
                    shake.keyBundle = createSecret(event.session!!)

                    if(shake.state == Async.READY){
                        val service = event.gatt!!.getService(SERVICE_UUID)
                        val char = service.getCharacteristic(REQUEST_UUID)
                        val json = Json.encodeToString(shake)
                        Log.v("ResolverHandler","Sending shake : $json")

                        char.value = json.toByteArray(CHARSET)
                        event.gatt.writeCharacteristic(char)
                    }

                }else{
                    val gatt = event.gatt!!
                    val service = gatt.getService(SERVICE_UUID)
                    val char = service.getCharacteristic(PROFILE_UUID)
                    gatt.setCharacteristicNotification (char, true)

                    val json = event.bytes.toString(CHARSET)

                    Log.v("ResolverHandler","Have shake : $json")


                    val shake = try{
                        Json.decodeFromString<HandShake>(json)
                    }catch(e : Exception){
                        Log.e(NETLOOPER_TAG,"handleResolverEvent: Error decoding handshake")
                        Log.e(NETLOOPER_TAG, json)
                        ret = 0
                        null
                    }

                    shake?.let{

                        val key = computeSharedKey(it,event.session!!)

                        if (key != null) {
                            Log.v("ResolverHandler","Have key : ${key.toBase64()}")
                        } else{
                            Log.v("ResolverHandler","Have key : null")
                        }


                        val sock = Socket(it.me, Socket.BLUETOOTH_GATT, null, gatt, event.device!!,null,key!!)
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

    @SuppressLint("MissingPermission")
    private suspend fun stopResolverConnection(event: ResolverEvent){
        Log.w(NETLOOPER_TAG,"handleGattEvent: Got disconnect event")
        if(event.socket == null){
            event.gatt?.disconnect()
        }else{
            Async.disconnectSocket(event.socket)
        }
    }

    private fun createSecret(session: Session) : KeyBundle{
        val signature = Signature()
        signature.init(PUBLIC_KEY, PRIVATE_KEY)

        val secret = session.generateSecret()
        val sig = signature.sign(secret)!!

        return KeyBundle(secret.toBase64(),sig.toBase64())
    }

    private fun computeSharedKey(shake: HandShake,session : Session) : ByteArray?{
        val publicKey = Ed25519PublicKeyParameters(shake.me.key.toByteArray())
        val secret = shake.keyBundle!!.secret
        val sig = shake.keyBundle!!.sig

        val verified = Signature().verify(secret.toByteArray(),sig.toByteArray(),publicKey)

        return if(verified){
            session.computeAgreement(secret.toByteArray())
        }else{
            Log.e(NETLOOPER_TAG,"handleResolverEvent: verification Failed")
            null
        }
    }


    private fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
    }

    private fun String.toByteArray() : ByteArray {
        return Base64.getDecoder().decode(this)
    }

}