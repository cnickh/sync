package daemon.dev.field.network

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import daemon.dev.field.*
import daemon.dev.field.bluetooth.GattResolver
import daemon.dev.field.network.handler.*
import kotlinx.coroutines.runBlocking

/**@brief this class implements a looper that takes blocking calls out of
 * network callbacks to prevent the loss of packets. All network callbacks/events are handled by
 * a unique type via a series of switch statements. All events are handled sequentially as
 * they are received.
 *
 * I would say the main job of this class is to handle the initial handshake and stop bad (out of order)
 * requests
 *
 * */

@SuppressLint("MissingPermission")
class NetworkLooper(val context : Context) : Thread(), Handler.Callback  {

    var mHandler: Handler? = null
    var mLooper: Looper? = null

    val gattHandler = GattEventHandler()
    val resHandler = ResolverEventHandler()

    val MAX_DISCONNNECTS : Int = 10
    val scannedDevices = mutableListOf<String>()
    val disconnectCount = hashMapOf<String,Int>()
    lateinit var switch: MeshService.NetworkSwitch

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
        while(mHandler==null){sleep(1)}
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
                    gattHandler.handleGattEvent(msg.obj as GattEvent)
                }
                RESOLVER -> {
                    val event = msg.obj as ResolverEvent
                    if(resHandler.handleResolverEvent(event)!=1){
                        event.device?.address?.let { suspendConnection(it) }
                    }
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
        if (getDevice(event.device.address) && (Async.state() == Async.READY)) {
            Log.i(NETLOOPER_TAG,"Connecting scanning ${event.device.address}")

            var gattCallback =
                GattResolver(event.device, getHandler())
            event.device.connectGatt(context, false, gattCallback)//, TRANSPORT_BREDR, PHY_LE_CODED)
        }

    }
}//NetLooper
