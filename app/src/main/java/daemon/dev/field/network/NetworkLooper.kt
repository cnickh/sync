package daemon.dev.field.network

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.*
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
 * a unique type (defined below) via a series of switch statements. All events are handled sequentially as
 * they are received.
 *
 * I would say the main job of this class is to handle the initial handshake and stop bad (out of order)
 * requests
 *
 * */

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
        const val RETRY = 7
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
    lateinit var switch: MeshService.NetworkSwitch

    fun addSwitch(switch: MeshService.NetworkSwitch){
        this.switch = switch
    }

    private fun suspendConnection(device : String){

        Handler(Looper.getMainLooper()).postDelayed({
            //Run delayed code here
            removeDev(device)
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
            Log.i(NETLOOPER_TAG,"Disconnecting from $device count ${disconnectCount[device]}")

            scannedDevices.remove(device)
            if(!disconnectCount.keys.contains(device)){
                Log.i(NETLOOPER_TAG,"disconnectCount[$device] = 1")
                disconnectCount[device] = 1
            }else{
                Log.i(NETLOOPER_TAG,"disconnectCount[$device]++")
                disconnectCount[device] = disconnectCount[device]!!+1
                Log.i(NETLOOPER_TAG,"count is now: ${disconnectCount[device]}")

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
        Log.i(NETLOOPER_TAG, "Killed Successfully")
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
            Log.i(NETLOOPER_TAG,"Connecting scanning ${event.device.address}")
            //switch.off()
            event.device.connectGatt(context, false, gattCallback)//, TRANSPORT_BREDR, PHY_LE_CODED)
        }

    }

    private suspend fun handleGattEvent(event : GattEvent){

        when(event.type){

            DISCONNECT ->{
                stopGattConnection(event)
            }
            PACKET ->{
                Log.i(NETLOOPER_TAG,"handleGattEvent: got PACKET")
                val socket = Async.getSocket(event.device)

                if(socket == null){
                    Log.i(NETLOOPER_TAG,"handleGattEvent: reading handshake")

                    val json = event.bytes!!.toString(CHARSET)
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

                        val sock = Socket(it.me, Socket.BLUETOOTH_DEVICE, null, null, event.device, event.gattServer)

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
                val json = Json.encodeToString(shake)
                if(shake.state == Async.READY){
                    Log.i(NETLOOPER_TAG,"Sending response $json")

                    event.gattServer?.sendResponse(
                        event.device, event.req!!, 0, 0, json.toByteArray(CHARSET))
                } else {
                    stopGattConnection(event)
                }
            }

        }//when(event.type)

    }//handlerGattEvent

    private suspend fun stopGattConnection(event: GattEvent){
        Log.w(NETLOOPER_TAG,"handleGattEvent: Got disconnect event")

        event.gattServer?.sendResponse(
            event.device, event.req!!, 300, 0, null)

        val sock = Async.getSocket(event.device)
        sock?.let{
            Async.disconnectSocket(it)
        }
        event.gattServer?.cancelConnection(event.device)
    }

    private suspend fun handleResolverEvent(event: ResolverEvent){

        when(event.type){

            RETRY ->{
                event.device?.address?.let { removeDev(it) }
                //switch.on()
            }
            DISCONNECT ->{
                stopResolverConnection(event)
            }
            PACKET ->{
                Async.receive(event.bytes!!,event.socket!!)
            }
            HANDSHAKE ->{
                Log.i(NETLOOPER_TAG,"handleResolverEvent: got HANDSHAKE")
                if(event.bytes == null){

                    val shake = Async.handshake()

                    if(shake.state == Async.READY){
                        val service = event.gatt!!.getService(SERVICE_UUID)
                        val char = service.getCharacteristic(REQUEST_UUID)
                        val json = Json.encodeToString(shake)
                        char.value = json.toByteArray(CHARSET)
                        event.gatt.writeCharacteristic(char)
                        Log.i(NETLOOPER_TAG,"handleResolverEvent: Handshake sent")

                    }

                }else{
                    Log.i(NETLOOPER_TAG,"handleResolverEvent: Socket being initialized")
                    val gatt = event.gatt!!
                    val service = gatt.getService(SERVICE_UUID)
                    val char = service.getCharacteristic(PROFILE_UUID)
                    gatt.setCharacteristicNotification (char, true)

                    val json = event.bytes.toString(CHARSET)
                    val shake = try{
                        Json.decodeFromString<HandShake>(json)
                    }catch(e : Exception){
                        Log.e(NETLOOPER_TAG,"handleResolverEvent: Error decoding handshake")
                        Log.e(NETLOOPER_TAG, json)
                        stopResolverConnection(event)
                        null
                    }

                    shake?.let{

                        val sock = Socket(it.me, Socket.BLUETOOTH_GATT, null, gatt, event.device!!,null)
                        event.res!!.socket = sock

                        if(!Async.connect(sock,it.me)){
                            Log.e(NETLOOPER_TAG,"handleResolverEvent: Too many peers canceling connect")
                            stopResolverConnection(event)
                        }

                    }
                }//if(event.bytes == null)

            }//HANDSHAKE

        }//when(event.type)

    }//handleResolverEvent

    private suspend fun stopResolverConnection(event: ResolverEvent){
        Log.w(NETLOOPER_TAG,"handleGattEvent: Got disconnect event")
        event.gatt?.disconnect()
        event.device?.address?.let { suspendConnection(it) }
        event.socket?.let{ Async.disconnectSocket(it) }

    }
}//NetLooper
