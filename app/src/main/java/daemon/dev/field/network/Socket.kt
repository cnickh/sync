package daemon.dev.field.network

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattServer
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import daemon.dev.field.PROFILE_UUID
import daemon.dev.field.REQUEST_UUID
import daemon.dev.field.SERVICE_UUID
import daemon.dev.field.SOCKET_TAG
import daemon.dev.field.nypt.Key
import kotlinx.coroutines.runBlocking
import java.net.Socket

@RequiresApi(Build.VERSION_CODES.O)
class Socket(
    val key : Key,
    val type : Int,
    private val socket: Socket?,
    private val gatt : BluetoothGatt?,
    val device : BluetoothDevice?
           ){

    companion object {
        const val BLUETOOTH_GATT=0
        const val BLUETOOTH_DEVICE=1
        const val WIFI=2
    }

    var sid : Int? = null
    lateinit var gattServer : BluetoothGattServer
    var open : Boolean = true

    init {

        when(type){
            BLUETOOTH_GATT -> {gatt!!}
            BLUETOOTH_DEVICE -> {device!!;gattServer=PeerRAM.getGatt()}
            WIFI -> {socket!!}
        }

        sid = runBlocking{PeerRAM.connect(this@Socket)}

        Log.d(SOCKET_TAG,"Peer[$sid] initialized successfully type[${type2String()}]")
    }

    fun write(buffer : ByteArray) {

        if(!open){
            return
        }

        Log.v(SOCKET_TAG,"type[${type2String()}], Write to $sid of size ${buffer.size}")
        when (type) {
            BLUETOOTH_GATT -> {
                writeGatt(buffer)
            }
            BLUETOOTH_DEVICE -> {
                writeDev(buffer)
            }
            WIFI -> {
                writeSock(buffer)
            }
        }

    }

    private fun writeSock(buffer : ByteArray){
        socket!!.getOutputStream().write(buffer)
    }

    private fun writeGatt(buffer : ByteArray){
        val service = gatt!!.getService(SERVICE_UUID)
        val char = service.getCharacteristic(REQUEST_UUID)
        char.value = buffer
        gatt.writeCharacteristic(char)
    }

    private fun writeDev(buffer : ByteArray){
        val service = gattServer.getService(SERVICE_UUID)
        val char = service.getCharacteristic(PROFILE_UUID)
        char.value = buffer
        gattServer.notifyCharacteristicChanged (device, char, true)
    }

    private fun type2String() : String{
        return when(type){
            BLUETOOTH_GATT -> {"BLUETOOTH_GATT"}
            BLUETOOTH_DEVICE -> {"BLUETOOTH_DEVICE"}
            WIFI -> {"WIFI"}
            else -> {"NONE???"}
        }
    }

    fun close(){

        open = false

        when (type) {
            BLUETOOTH_GATT -> {
                gatt?.disconnect()
                gatt?.close()
            }
            BLUETOOTH_DEVICE -> {
                gattServer.cancelConnection(device)
            }
            WIFI -> {
                //TODO
            }
        }
    }

}