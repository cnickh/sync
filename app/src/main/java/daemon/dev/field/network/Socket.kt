package daemon.dev.field.network

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattServer
import android.os.Build
import android.os.ConditionVariable
import android.util.Log
import androidx.annotation.RequiresApi
import daemon.dev.field.*
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.network.util.Packer
import kotlinx.coroutines.runBlocking
import java.net.Socket

@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.O)
class Socket(
    val user : User,
    val type : Int,
    private val socket: Socket?,
    private val gatt : BluetoothGatt?,
    val device : BluetoothDevice,
    val gattServer : BluetoothGattServer?
           ){

    companion object {
        const val BLUETOOTH_GATT=0
        const val BLUETOOTH_DEVICE=1
        const val WIFI=2
    }

    var key : String
    var open : Boolean = true
    val response = ConditionVariable()


    override fun equals(other: Any?): Boolean {
        val s = other as daemon.dev.field.network.Socket
        if(s.key != key){return false}
        if(s.type != type){return false}
        return true
    }

    init {

        when(type){
            BLUETOOTH_GATT -> {gatt!!}
            BLUETOOTH_DEVICE -> {device!!;gattServer!!}
            WIFI -> {socket!!}
        }

        key = user.key

        Log.d(SOCKET_TAG,"Peer initialized successfully type[${type2String()}]")
    }

    fun write(buffer : ByteArray) {

        if(!open){
            Log.e(SOCKET_TAG,"err closed socket")
            return
        }

        Log.v(SOCKET_TAG,"type[${type2String()}], Write to  of size ${buffer.size}")
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
        Log.d("Async","write GATT")

        val service = gatt!!.getService(SERVICE_UUID)
        val char = service.getCharacteristic(REQUEST_UUID)
        char.value = buffer
        gatt.writeCharacteristic(char)
    }

    private fun writeDev(buffer : ByteArray){
        Log.d("Async","write BT_DEVICE $device")

        val service = gattServer?.getService(SERVICE_UUID)
        val char = service?.getCharacteristic(PROFILE_UUID)
        char?.value = buffer
        char?.let{
            gattServer?.notifyCharacteristicChanged (device, char, true)
        }

    }

    fun type2String() : String{
        return when(type){
            BLUETOOTH_GATT -> {"BLUETOOTH_GATT"}
            BLUETOOTH_DEVICE -> {"BLUETOOTH_DEVICE"}
            WIFI -> {"WIFI"}
            else -> {"NONE???"}
        }
    }

    fun close(){

        Log.i("Socket.kt","Close called on ${key}")

        open = false

        when (type) {
            BLUETOOTH_GATT -> {
                gatt?.disconnect()
                gatt?.close()
            }
            BLUETOOTH_DEVICE -> {
                gattServer?.cancelConnection(device)
            }
            WIFI -> {
                //TODO
            }
        }
    }

    fun send(packer : Packer){

        var buffer = packer.next()

        var count = 0
        while(buffer != null){
            Log.i("Async.kt", "sending packet $count / ${packer.count()}")

            write(buffer)
            buffer = packer.next()

            if(!response.block(BLE_INTERVAL)){
                Log.e("Async.kt","response timeout for $count / ${packer.count()}")
            }

            response.close()
            count++
        }

    }

}