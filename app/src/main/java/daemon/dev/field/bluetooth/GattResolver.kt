package daemon.dev.field.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.os.*
import android.util.Log
import daemon.dev.field.*
import daemon.dev.field.cereal.objects.HandShake
import daemon.dev.field.cereal.objects.KeyBundle
import daemon.dev.field.network.util.NetworkEventDefinition.*
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.RESOLVER
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.DISCONNECT
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.RETRY
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.CONNECT
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.PACKET
import daemon.dev.field.network.Socket
import daemon.dev.field.nypt.Session
import daemon.dev.field.nypt.Signature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import java.util.Base64


@SuppressLint("MissingPermission")
class GattResolver(val device : BluetoothDevice, val handler: Handler, val shake: HandShake) : BluetoothGattCallback() {

        var socket : Socket? = null
        var remoteHost : ByteArray? = null
        var session : Session? = null
        var die = false

    private fun createSecret(session: Session) : KeyBundle {
        val signature = Signature()
        signature.init(PUBLIC_KEY, PRIVATE_KEY)

        val secret = session.generateSecret()
        val sig = signature.sign(secret)!!

        return KeyBundle(secret.toBase64(),sig.toBase64())
    }

    private fun computeSharedKey(shake: HandShake, session : Session) : ByteArray?{
        val publicKey = Ed25519PublicKeyParameters(shake.me.key.toByteArray())
        val secret = shake.keyBundle!!.secret
        val sig = shake.keyBundle!!.sig

        val verified = Signature().verify(secret.toByteArray(),sig.toByteArray(),publicKey)

        return if(verified){
            session.computeAgreement(secret.toByteArray())
        }else{
            Log.e(GATT_RESOLVER_TAG,"handleResolverEvent: verification Failed")
            null
        }
    }

    private var connected : Boolean = false
    private val connectLock = Mutex()
    private suspend fun connect(){
        connectLock.lock()

        connected = true

        connectLock.unlock()
    }


    private suspend fun check(){
        connectLock.lock()

        if(!connected){
            sendEvent(RETRY,null, null, device)
            die = true
        }

        connectLock.unlock()
    }


    private fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
    }

    private fun String.toByteArray() : ByteArray {
        return Base64.getDecoder().decode(this)
    }


        private fun sendEvent(type : Int,
                              bytes : ByteArray?,
                              socket : Socket?,
                              device: BluetoothDevice?
        ){

            handler.obtainMessage(RESOLVER,
                ResolverEvent(type,bytes,socket,device)).sendToTarget()
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            Log.d(GATT_RESOLVER_TAG, "onConnectionStateChange: @${device.address} success: $isSuccess connected: $isConnected")
            if(die){Log.e(GATT_RESOLVER_TAG,"onConnectionStateChange called in dead resolver")}

            if (isSuccess && isConnected) {
                gatt.requestMtu(MTU)

                CoroutineScope(Dispatchers.IO).launch {
                    delay(CONFIRMATION_TIMEOUT) //10 seconds
                    check()
                }

            } else {
                Log.e(GATT_RESOLVER_TAG,"onConnectionStateChange was bad am resolver time to forget :p")
                //remoteHost = null
                sendEvent(RETRY,null, null, device)
                die = true
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if(die){Log.e(GATT_RESOLVER_TAG,"onMtuChanged called in dead resolver")}
            Log.d(GATT_RESOLVER_TAG,"onMtuChanged success")
            gatt?.discoverServices()
        }

        override fun onServicesDiscovered(discoveredGatt: BluetoothGatt, status: Int) {
            if(die){Log.e(GATT_RESOLVER_TAG,"onServicesDiscovered called in dead resolver")}

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(GATT_RESOLVER_TAG, "onServicesDiscovered: Have gatt $discoveredGatt")

                val service = discoveredGatt.getService(SERVICE_UUID)
                val pChar = service?.getCharacteristic(PROFILE_UUID)
                discoveredGatt.setCharacteristicNotification(pChar, true)

                if(service == null){
                    Log.e(GATT_RESOLVER_TAG,"service null")
                    //socket?.let { sendEvent(DISCONNECT,null, it, null) }
                } else {
                        Log.d(GATT_RESOLVER_TAG, "Reading Characteristic")
                    val characteristic = service.getCharacteristic(PROFILE_UUID)
                    discoveredGatt.readCharacteristic(characteristic)
                }


            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int,
        ) {
            if(die){Log.e(GATT_RESOLVER_TAG,"onCharacteristicRead called in dead resolver")}

            Log.d(GATT_RESOLVER_TAG, "onCharacteristicRead called $status")

            remoteHost = characteristic?.value
            Log.d(GATT_RESOLVER_TAG,"have char $characteristic")
            Log.d(GATT_RESOLVER_TAG, "have remotehost $remoteHost")

            session = Session()
            shake.keyBundle = createSecret(session!!)
            val publicKey = Ed25519PublicKeyParameters(PUBLIC_KEY)

            val verified = Signature().verify(shake.keyBundle!!.secret.toByteArray(),
                shake.keyBundle!!.sig.toByteArray(), publicKey)

            Log.v(GATT_RESOLVER_TAG,"verification: $verified")

            val service = gatt?.getService(SERVICE_UUID)
            val char = service?.getCharacteristic(REQUEST_UUID)
            val json = Json.encodeToString(shake)
            Log.v(GATT_RESOLVER_TAG,"Sending shake : $json")

            if (char != null) {
                char.value = json.toByteArray(CHARSET)
                gatt.writeCharacteristic(char)
            }

//            if(shake.state == Async.READY){
//                val service = gatt?.getService(SERVICE_UUID)
//                val char = service?.getCharacteristic(REQUEST_UUID)
//                val json = Json.encodeToString(shake)
//                Log.v(GATT_RESOLVER_TAG,"Sending shake : $json")
//
//                if (char != null) {
//                    char.value = json.toByteArray(CHARSET)
//                    gatt.writeCharacteristic(char)
//                }
//            }

        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int,
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if(die){Log.e(GATT_RESOLVER_TAG,"onCharacteristicWrite called in dead resolver")}

            Log.d(
                GATT_RESOLVER_TAG,
                "onCharacteristicWrite called w/ status[$status] remoteHost: $remoteHost"
            )

            socket?.response?.open()

            remoteHost?.let { bytes ->

                val json = bytes.toString(CHARSET)
                Log.v(GATT_RESOLVER_TAG, "Have shake : $json")

                val shake = try {
                    Json.decodeFromString<HandShake>(json)
                } catch (e: Exception) {
                    null
                }

                shake?.let {

                    val key = computeSharedKey(it, session!!)
                    Log.i(GATT_RESOLVER_TAG, "Resolver establishing key ${key!!.toBase64()}")
                    val sock = Socket(
                        it.me,
                        Socket.BLUETOOTH_GATT,
                        null,
                        gatt,
                        device,
                        null,
                        key!!
                    )
                    socket = sock
                    remoteHost = null

                    sendEvent(CONNECT,null,sock,null)
                    runBlocking { connect() }
                }

            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            if(die){Log.e(GATT_RESOLVER_TAG,"onCharacteristicChanged called in dead resolver")}

            Log.d(GATT_RESOLVER_TAG, "onCharacteristicChanged called")
            var data: ByteArray? = null

            if(socket!=null) {

                data = characteristic?.value

                data?.let { bytes ->
                    sendEvent(PACKET,bytes, socket!!,null)
                }

            } else {
            //do something
                Log.d(GATT_RESOLVER_TAG, "onCharacteristicChanged called with socket null")
            }


        }
}

