package daemon.dev.field.network

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattServer
import android.os.Build
import android.os.ConditionVariable
import android.util.Log
import android.util.Range
import androidx.annotation.RequiresApi
import daemon.dev.field.*
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.network.util.Packer
import daemon.dev.field.nypt.Symmetric
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
    private val gattServer : BluetoothGattServer?,
    private val symKey : ByteArray
           ){

    companion object {
        const val BLUETOOTH_GATT=0
        const val BLUETOOTH_DEVICE=1
        const val WIFI=2
    }

    var key : String
    var open : Boolean = true
    val response = ConditionVariable()
    private var symmetric : Symmetric = Symmetric()


    override fun equals(other: Any?): Boolean {
        val s = other as daemon.dev.field.network.Socket
        if(s.key != key){return false}
        if(s.type != type){return false}
        return true
    }

    init {

        symmetric.init(symKey)

        when(type){
            BLUETOOTH_GATT -> {gatt!!}
            BLUETOOTH_DEVICE -> {gattServer!!}
            WIFI -> {socket!!}
        }

        key = user.key

//        Log.d(SOCKET_TAG,"Peer initialized successfully type[${type2String()}]")
    }

    fun decrypt(bytes: ByteArray) : ByteArray {
        return symmetric.decrypt(bytes)
    }

    fun write(buffer : ByteArray) : Int {

        val cipher = symmetric.encrypt(buffer)

        val r = IntRange(0,16)
        val subc = cipher.slice(r)
        val subp = buffer.slice(r)

//        Log.v(SOCKET_TAG,"Have $subp plain & \n $subc cipher")

        if(!open){
            Log.e(SOCKET_TAG,"err closed socket")
            return -1
        }
        try {
//            Log.v(SOCKET_TAG, "type[${type2String()}], Write to  of size ${buffer.size}")
            when (type) {
                BLUETOOTH_GATT -> {
                    writeGatt(cipher)
                }
                BLUETOOTH_DEVICE -> {
                    writeDev(cipher)
                }
                WIFI -> {
                    writeSock(cipher)
                }
            }
        } catch(e : Exception){
            return -1
        }

        return 0
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

        Log.i(SOCKET_TAG,"Close called on $key")

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

    fun send(packer : Packer): Int{

        var buffer = packer.next()

        var count = 0
        while(buffer != null){
//            Log.i(SOCKET_TAG, "sending packet $count / ${packer.count()}")

            if(write(buffer) != 0){
                Log.e(SOCKET_TAG, "Aborting write socket closed")
                return -1
            }
            buffer = packer.next()

            if(!response.block(BLE_INTERVAL)){
                Log.e(SOCKET_TAG,"response timeout for $count / ${packer.count()}")
            }

            response.close()
            count++
        }

        return 0
    }

}