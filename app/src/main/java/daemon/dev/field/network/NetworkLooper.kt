package daemon.dev.field.network

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattServer
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import daemon.dev.field.*
import daemon.dev.field.bluetooth.GattResolver
import daemon.dev.field.cereal.objects.HandShake
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.User
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**@brief this class implements a looper and message handler that takes blocking calls out of
 * network callbacks to prevent the loss of packets. All network callbacks/events are handled by
 * type as define below via a series of switch statements. All events are handled sequentially as
 * they are received. */

@SuppressLint("MissingPermission")
class NetworkLooper(val context : Context) : Thread(), Handler.Callback  {

    companion object {
        const val SCANNER = 0
        const val GATT = 1
        const val RESOLVER = 2
        const val APP = 3

        const val HANDSHAKE = 4
        const val PACKET = 5
        const val DISCONNECT = 6
    }

    data class ScanEvent(val device : BluetoothDevice)

    data class GattEvent(val type : Int,
        val device: BluetoothDevice, val bytes : ByteArray?, val gattServer: BluetoothGattServer?, val req : Int?)

    data class ResolverEvent(val type : Int,
        val socket: Socket?, val bytes : ByteArray?, val device: BluetoothDevice?, val gatt: BluetoothGatt?, val res : GattResolver?)

    data class AppEvent(val socket : Socket)

    var mHandler: Handler? = null
    var mLooper: Looper? = null

    val MAX_DISCONNNECTS : Int = 10
    val scannedDevices = mutableListOf<String>()
    val disconnectCount = hashMapOf<String,Int>()

    private fun suspendConnection(device : String){

        Handler(Looper.getMainLooper()).postDelayed({
            //Run delayed code here
            scannedDevices.remove(device)
        }, CONFIRMATION_TIMEOUT)

    }

    private fun getDevice(device: String) : Boolean{

        if(disconnectCount.containsKey(device)){
            if(disconnectCount[device]!! >= MAX_DISCONNNECTS){
                return false
            }
        }

        return if (scannedDevices.contains(device)) {
            false
        } else {
            scannedDevices.add(device)
            true
        }
    }

    fun removeDev(device : String){

        if (scannedDevices.contains(device)) {
            Log.i("NetworkLooper.kt","Disconnecting from $device count ${disconnectCount[device]}")

            scannedDevices.remove(device)
            if(!disconnectCount.keys.contains(device)){
                Log.i("NetworkLooper.kt","disconnectCount[$device] = 1")
                disconnectCount[device] = 1
            }else{
                Log.i("NetworkLooper.kt","disconnectCount[$device]++")
                disconnectCount[device] = disconnectCount[device]!!+1
                Log.i("NetworkLooper.kt","count is now: ${disconnectCount[device]}")

            }
        }
    }

    fun getHandler() : Handler {
        while(mHandler==null){}
        return mHandler!!
    }

    override fun run() {
        Looper.prepare()
        mLooper = Looper.myLooper()
        mHandler = Handler(mLooper!!,this)
        Looper.loop()
        Log.i("Looper.kt", "Killed Successfully")
    }

    override fun handleMessage(msg: Message): Boolean {
        runBlocking {
            when (msg.what) {
                SCANNER -> {
                    handleScanEvent(msg.obj as ScanEvent)
                }
                GATT -> {
                    handleGattEvent(msg.obj as GattEvent)
                }
                RESOLVER -> {
                    handleResolverEvent(msg.obj as ResolverEvent)
                }
                APP -> {
                    handleAppEvent(msg.obj as AppEvent)
                }
            }
        }
        return false
    }

    fun kill(){
        mLooper?.quit()
    }

    private fun handleAppEvent(event: AppEvent){
        removeDev(event.socket.device.address)
    }

    private suspend fun handleScanEvent(event : ScanEvent){

        var gattCallback =
            GattResolver(event.device, getHandler())

        if (getDevice(event.device.address) && (Async.state() == Async.READY)) {
            event.device.connectGatt(context, false, gattCallback)
        }

    }

    private suspend fun handleGattEvent(event : GattEvent){

        when(event.type){

            DISCONNECT ->{
                Log.i("handleGattEvent","Got disconnect event")
                val sock = Async.getSocket(event.device)
                sock?.let{
                    Async.disconnectSocket(it)
                }
            }
            PACKET ->{

                event.gattServer?.sendResponse(
                    event.device, event.req!!, 0, 0, null)

                val socket = Async.getSocket(event.device)

                if(socket == null){

                    val json = event.bytes!!.toString(CHARSET)
                    val shake = try{
                         Json.decodeFromString<HandShake>(json)
                    }catch(e : Exception){
                        Log.e("NetLooper","handleGattEvent: Error decoding handshake")
                        Log.e("NetLooper","$json")

                        null
                    }

                    shake?.let {
                        val sock = Socket(it.me, Socket.BLUETOOTH_DEVICE, null, null, event.device, event.gattServer)

                        if(it.state == Async.READY){

                            val connected = Async.connect(sock,it.me)

                            if(!connected){
                                event.gattServer?.cancelConnection(event.device)
                            }else{}

                        } else {
                            event.gattServer?.cancelConnection(event.device)
                        }

                    }

                }else{
                    event.bytes?.let { Async.receive(it, socket) }
                }
            }
            HANDSHAKE ->{
                val json = Json.encodeToString(Async.handshake())
                event.gattServer?.sendResponse(
                    event.device, event.req!!, 0, 0, json.toByteArray(CHARSET))
            }

        }

    }

    private suspend fun handleResolverEvent(event: ResolverEvent){

        when(event.type){
            DISCONNECT ->{
                Log.i("handleResolverEvent","Got disconnect event")
                event.device?.address?.let { removeDev(it) }
                event.socket?.let{Async.disconnectSocket(it)}
            }
            PACKET ->{
                Async.receive(event.bytes!!,event.socket!!)
            }
            HANDSHAKE ->{
                Log.i("NetworkLooper.kt","handleGattEvent: ResolverHandshake called")
                if(event.bytes == null){
                    val service = event.gatt!!.getService(SERVICE_UUID)
                    val char = service.getCharacteristic(REQUEST_UUID)
                    val json = Json.encodeToString(Async.handshake())
                    char.value = json.toByteArray(CHARSET)
                    event.gatt.writeCharacteristic(char)
                }else{
                    Log.i("NetworkLooper.kt","handleResolverEvent: Socket being initialized")
                    val gatt = event.gatt!!
                    val service = gatt.getService(SERVICE_UUID)
                    val char = service.getCharacteristic(PROFILE_UUID)
                    gatt.setCharacteristicNotification (char, true)

                    val json = event.bytes.toString(CHARSET)
                    val shake = try{
                        Json.decodeFromString<HandShake>(json)
                    }catch(e : Exception){
                        Log.e("NetLooper","handleResolverEvent: Error decoding handshake")
                        Log.e("NetLooper","$json")
                        null
                    }
                    shake?.let{

                        val sock = Socket(it.me, Socket.BLUETOOTH_GATT, null, gatt, event.device!!,null)
                        event.res!!.socket = sock
                        if(it.state == Async.READY){

                            val connected = Async.connect(sock,it.me)

                            if(!connected){
                                gatt.disconnect()
                                event.device.address?.let { suspendConnection(it) }
                            }else{}

                        } else {
                            gatt.disconnect()
                            event.device.address?.let { suspendConnection(it) }
                        }


                    }
                }

            }

        }

    }
}
