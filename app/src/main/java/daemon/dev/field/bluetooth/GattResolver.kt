package daemon.dev.field.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import daemon.dev.field.*
import daemon.dev.field.network.Async
import daemon.dev.field.network.handler.event.*
import daemon.dev.field.network.Socket


@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.O)
class GattResolver(val device : BluetoothDevice, val handler: Handler) : BluetoothGattCallback() {
        var socket : Socket? = null
        var remoteHost : ByteArray? = null

        private fun sendEvent(type : Int,socket : Socket?, bytes : ByteArray?, device: BluetoothDevice?, gatt: BluetoothGatt?, res : GattResolver?){
            handler.obtainMessage(RESOLVER,
                ResolverEvent(type,socket,bytes,device,gatt,res)).sendToTarget()
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            Log.d(GATT_RESOLVER_TAG, "onConnectionStateChange: @${device.address} success: $isSuccess connected: $isConnected")

            if (isSuccess && isConnected) {
                gatt.requestMtu(MTU)
            } else {
                Log.e(GATT_RESOLVER_TAG,"onConnectionStateChange was bad am resolver time to forget :p")
                remoteHost = null
                sendEvent(RETRY,socket,null,device,null,null)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            gatt?.discoverServices()
        }

        override fun onServicesDiscovered(discoveredGatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.v(GATT_RESOLVER_TAG, "onServicesDiscovered: Have gatt $discoveredGatt")

                try {

                    val service = discoveredGatt.getService(SERVICE_UUID)

                    if(service == null){
                        Log.e(GATT_RESOLVER_TAG,"service null")
                        sendEvent(DISCONNECT,socket,null,device,null,null)
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
            Log.v(GATT_RESOLVER_TAG, "onCharacteristicRead called $status")

            try {

                remoteHost = characteristic?.value
                Log.v(GATT_RESOLVER_TAG,"have char $characteristic")
                Log.v(GATT_RESOLVER_TAG, "have remotehost $remoteHost")

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

//            Log.i(GATT_RESOLVER_TAG,"onCharacteristicWrite called w/ status[$status]\n $remoteHost")

            if(status != 200){
                Log.e(GATT_RESOLVER_TAG,"got 300 sending disconnect")
                sendEvent(DISCONNECT,socket,null,device,null,null)
            }

            socket?.let{
                it.response.open()
            }

            remoteHost?.let { bytes ->
                Log.i(GATT_RESOLVER_TAG,"Handshake with bytes being sent :/")

                sendEvent(HANDSHAKE,null,bytes,device,gatt,this)

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

