package daemon.dev.field.network.handler.event

import android.annotation.SuppressLint
import android.util.Log
import daemon.dev.field.CHARSET
import daemon.dev.field.NETLOOPER_TAG
import daemon.dev.field.PRIVATE_KEY
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.cereal.objects.HandShake
import daemon.dev.field.cereal.objects.KeyBundle
import daemon.dev.field.network.Async
import daemon.dev.field.network.Socket
import daemon.dev.field.nypt.Session
import daemon.dev.field.nypt.Signature
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import java.util.*

class  GattEventHandler {

    @SuppressLint("MissingPermission")
    suspend fun handleGattEvent(event : GattEvent){

        when(event.type){

            DISCONNECT ->{
                stopGattConnection(event)
            }
            PACKET ->{
                val socket = Async.getSocket(event.device)

                if(socket == null){
                    val json = event.bytes!!.toString(CHARSET)

                    Log.v("GattHandler","Have shake : $json")


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

                        Log.v("GattHandler","Have shake $it and eventSession ${event.session!!}")
                        val key = computeSharedKey(it, event.session)

                        if (key != null) {
                            Log.v("GattHandler","Have key : ${key.toBase64()}")
                        } else{
                            Log.v("GattHandler","Have key : null")
                        }

                        val sock = Socket(it.me, Socket.BLUETOOTH_DEVICE, null, null, event.device, event.gattServer,key!!)

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
                shake.keyBundle = createSecret(event.session!!)
                val json = Json.encodeToString(shake)
                val publicKey = Ed25519PublicKeyParameters(PUBLIC_KEY)

                val verified = Signature().verify(shake.keyBundle!!.secret.toByteArray(),
                    shake.keyBundle!!.sig.toByteArray(), publicKey)

                Log.v("GattHandler","verification: $verified")
                Log.v("GattHandler","Sending shake : $json")

                if(shake.state == Async.READY){
                    event.gattServer?.sendResponse(
                        event.device, event.req!!, 0, 0, json.toByteArray(CHARSET))
                } else {
                    stopGattConnection(event)
                }
            }

        }//when(event.type)

    }//handlerGattEvent

    @SuppressLint("MissingPermission")
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
            Log.e(NETLOOPER_TAG,"GattHandlerEvent: verification failed")
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