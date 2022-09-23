package daemon.dev.field.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import daemon.dev.field.*
import daemon.dev.field.cereal.objects.HandShake
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.network.Async
import daemon.dev.field.network.NetworkLooper
import daemon.dev.field.network.NetworkLooper.Companion.DISCONNECT
import daemon.dev.field.network.NetworkLooper.Companion.HANDSHAKE
import daemon.dev.field.network.NetworkLooper.Companion.PACKET
import daemon.dev.field.network.Socket
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.O)
class GattResolver(val address : String, val handler: Handler) : BluetoothGattCallback() {
        var socket : Socket? = null
        var remoteHost : ByteArray? = null

        private fun sendEvent(type : Int,socket : Socket?, bytes : ByteArray?, address: String?, gatt: BluetoothGatt?, res : GattResolver?){
            handler.obtainMessage(NetworkLooper.RESOLVER,
                NetworkLooper.ResolverEvent(type,socket,bytes,address,gatt,res)).sendToTarget()
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            Log.d(GATT_RESOLVER_TAG, "onConnectionStateChange: @$address success: $isSuccess connected: $isConnected")

            if (isSuccess && isConnected) {
                gatt.requestMtu(MTU)
            } else {
                sendEvent(DISCONNECT,socket,null,address,null,null)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            gatt?.discoverServices()
        }

        override fun onServicesDiscovered(discoveredGatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.v(GATT_RESOLVER_TAG, "onServicesDiscovered: Have gatt $discoveredGatt")

                try {

                    val service = discoveredGatt.getService(SERVICE_UUID)

                    if(service == null){
                        sendEvent(DISCONNECT,socket,null,address,null,null)
                    } else {
//                        Log.d(GATT_RESOLVER_TAG, "Reading Characteristic")
                        val characteristic = service.getCharacteristic(PROFILE_UUID)
                        discoveredGatt.readCharacteristic(characteristic)
                    }

                } catch (e : Exception){

                    val msg = e.message
                    Log.e(GATT_RESOLVER_TAG,"Error onServicesDiscovered: $msg ")

                }

            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int,
        ) {
//            Log.v(GATT_RESOLVER_TAG, "onCharacteristicRead called")

            try {

                remoteHost = characteristic?.value

                sendEvent(HANDSHAKE,null,null,null,gatt!!,null)

            } catch (e : Exception){
                val msg = e.message
                val cas = e.cause
                Log.e(GATT_RESOLVER_TAG,"Error onCharacteristicRead: $msg : $cas")
            }

        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int,
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)

            Log.i(GATT_RESOLVER_TAG,"onCharacteristicWrite called")

            socket?.let{ _ ->
                Async.response.open()
            }

            remoteHost?.let { bytes ->

                sendEvent(HANDSHAKE,null,bytes,null,gatt,this)

            }
            remoteHost = null
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
//            Log.d(GATT_RESOLVER_TAG, "onCharacteristicChanged called")
            var data: ByteArray? = null

            try {

                if(socket!=null) {

                    data = characteristic?.value

                    data?.let { bytes ->
                        sendEvent(PACKET,socket,bytes,null,null,null)
                    }

                } else {
                    //do something
                }

            } catch (e : Exception){
                val msg = e.message
                val cas = e.cause
                Log.e(GATT_RESOLVER_TAG,"Error onCharacteristicChanged: $msg : $cas")
            }
//            Log.d(GATT_RESOLVER_TAG, "onCharacteristicChanged: Have ${data?.toString(Charsets.US_ASCII)}")
        }
}

