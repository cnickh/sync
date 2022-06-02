package daemon.dev.field.bluetooth

import android.bluetooth.*
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import daemon.dev.field.*
import daemon.dev.field.data.PostRAM
import daemon.dev.field.data.objects.RemoteHost
import daemon.dev.field.network.PeerRAM
import daemon.dev.field.network.Socket
import daemon.dev.field.util.Serializer
import kotlinx.coroutines.runBlocking
import java.nio.charset.Charset

@RequiresApi(Build.VERSION_CODES.O)
class GattResolver(val addr : String, val scanner: BluetoothScanner, val resolver : Handler) : BluetoothGattCallback() {
        private val serializer = Serializer()
        var sid : Int? = null
        private val host : RemoteHost = RemoteHost(null, PostRAM.me.uid)
        var remoteHost : ByteArray? = null


        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {

            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            Log.d(GATT_RESOLVER_TAG, "onConnectionStateChange: @$addr success: $isSuccess connected: $isConnected")

            // try to send a message to the other device as a test
            if (isSuccess && isConnected) {
                // discover services
                gatt.requestMtu(MTU)

            } else {
                Log.d(GATT_RESOLVER_TAG,"Failed with status = $status and state = $newState")
                Log.d(GATT_RESOLVER_TAG,"Freeing address")

                runBlocking {
                    sid?.let { PeerRAM.disconnect(it,Socket.BLUETOOTH_GATT);sid==null }
                    scanner.removeDev(addr)
                }
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
                    Log.v(GATT_RESOLVER_TAG, "onServicesDiscovered: Have service $service")

                    if(service == null){
                        runBlocking {
                            sid?.let { PeerRAM.disconnect(it,Socket.BLUETOOTH_GATT);sid==null }
                            scanner.removeDev(addr)
                        }
                    } else {
                        Log.d(GATT_RESOLVER_TAG, "Reading Characteristic")
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
            Log.v(GATT_RESOLVER_TAG, "onCharacteristicRead called")
            var data : ByteArray? = null

            try {

                remoteHost = characteristic?.value

                Log.d(GATT_RESOLVER_TAG, "Sending handshake packet")
                val service = gatt!!.getService(SERVICE_UUID)
                val char = service.getCharacteristic(REQUEST_UUID)
                char.value = serializer.hostToByte(host)
                gatt.writeCharacteristic(char)


            } catch (e : Exception){
                val msg = e.message
                val cas = e.cause
                Log.e(GATT_RESOLVER_TAG,"Error onCharacteristicRead: $msg : $cas")
            }
            Log.d(GATT_RESOLVER_TAG, "onCharacteristicRead: Have $data")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int,
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)

            Log.v(GATT_RESOLVER_TAG,"onCharacteristicWrite called")


            sid?.let{ _ ->
                PeerRAM.res.open()
            }

            remoteHost?.let { bytes ->

                val service = gatt!!.getService(SERVICE_UUID)
                val char = service.getCharacteristic(PROFILE_UUID)
                gatt.setCharacteristicNotification (char, true)

                serializer.getHost(bytes).let{
                    val peer = Socket(it.uid, Socket.BLUETOOTH_GATT, null, gatt, null)
                    sid = peer.sid
                }

            }
            remoteHost = null
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.d(GATT_RESOLVER_TAG, "onCharacteristicChanged called sid:$sid")
            var data: ByteArray? = null

            try {

                if(sid!=null) {

                    data = characteristic?.value

                    data?.let { bytes ->
                        resolver.obtainMessage(sid!!, 0, 0,
                            bytes).sendToTarget()
                    }
                } else {
                    //do something
                }

            } catch (e : Exception){
                val msg = e.message
                val cas = e.cause
                Log.e(GATT_RESOLVER_TAG,"Error onCharacteristicChanged: $msg : $cas")
            }
            Log.d(GATT_RESOLVER_TAG, "onCharacteristicChanged: Have ${data?.toString(Charset.defaultCharset())}")
        }
}

