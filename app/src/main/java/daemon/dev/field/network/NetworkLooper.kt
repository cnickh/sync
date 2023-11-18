package daemon.dev.field.network

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice.*
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.compose.material.contentColorFor
import daemon.dev.field.*
import daemon.dev.field.bluetooth.Gatt
import daemon.dev.field.bluetooth.GattResolver
import daemon.dev.field.cereal.objects.HandShake
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.network.handler.SyncOperator
import daemon.dev.field.network.util.NetworkEventDefinition
import daemon.dev.field.network.util.NetworkEventDefinition.*
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.GATT
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.RESOLVER
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.APP
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.SCANNER
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.CONNECT
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.PACKET
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.DISCONNECT
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.RETRY
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.STATE
import daemon.dev.field.network.util.Packer
import daemon.dev.field.network.util.PeerNetwork
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
class NetworkLooper(val context : Context, val profile : User) : Thread(), Handler.Callback  {

    var mHandler: Handler? = null
    var mLooper: Looper? = null

    val MAX_DISCONNNECTS : Int = 99
    val scannedDevices = mutableListOf<String>()
    val disconnectCount = hashMapOf<String,Int>()

    val op = SyncOperator(context)
    val pn = PeerNetwork()

    private lateinit var gatt : Gatt
    private val netDef = NetworkEventDefinition()

    fun setGatt(gatt : Gatt){
        this.gatt = gatt
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
            //Async.addDevice(device)
            true
        }
    }

    private fun removeDev(device : String){

        if (scannedDevices.contains(device)) {
            Log.i(NETLOOPER_TAG,"Disconnecting from $device count ${disconnectCount[device]}")

            scannedDevices.remove(device)
            //Async.remDevice(device)

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
        updateMain(STATE,"READY")
        Looper.prepare()
        mLooper = Looper.myLooper()
        mHandler = Handler(mLooper!!,this)
        Looper.loop()
        Log.i(NETLOOPER_TAG, "Killed Successfully")
        updateMain(STATE,"IDLE")
    }

    override fun handleMessage(msg: Message): Boolean {

        when (msg.what) {
            SCANNER -> {
                val scannerEvent = msg.obj as ScanEvent
                handleScanEvent(scannerEvent)
            }
            GATT -> {
                val gattEvent = msg.obj as GattEvent
                handleGattEvent(gattEvent)
            }
            RESOLVER -> {
                val resolverEvent = msg.obj as ResolverEvent
                handleResolverEvent(resolverEvent)
            }
            APP -> {
                handleAppEvent(msg.obj as AppEvent)
            }
        }
        return false
    }

    fun kill(){
        mLooper?.quit()
    }

    private fun handleAppEvent(event: AppEvent){
        Log.i(NETLOOPER_TAG,"Handing AppEvent")

        pn.gattConnection(event.key)?.let {
            Log.i(NETLOOPER_TAG,"Have GATT for ${event.key}")

            val packer = Packer(event.raw)
            if (it.send(packer)==-1){
                val res = it.connect()
                Log.i(NETLOOPER_TAG,"gatt.connect() : $res")
                if(res){
                    val send = it.send(Packer(event.raw))
                    Log.i(NETLOOPER_TAG,"send 2nd attempt : $send")
                } else {
                    pn.closeSocket(it)
                    val user = it.user
                    val intent = Intent(netDef.code2String(DISCONNECT))
                    intent.putExtra("extra", Json.encodeToString(user))
                    context.sendBroadcast(intent)
                }
            }
        }
    }

    private fun handleScanEvent(event : ScanEvent){

        if (getDevice(event.result.device.address)) {
            Log.i(NETLOOPER_TAG,"Connecting scanning ${event.result.device.address}")
            val gattCallback =
                GattResolver(event.result.device, getHandler(),HandShake(0,profile,null,null))
            event.result.device.connectGatt(context, false, gattCallback, TRANSPORT_AUTO, PHY_LE_CODED)
            updateMain(SCANNER,event.result.device.address)
        }

    }


    private fun handleGattEvent(event : GattEvent){

        when(event.type){
            CONNECT ->{
                pn.add(event.socket!!)
                pn.print_state()
            }
            PACKET ->{
                val socket = event.socket!!
                val res = op.receive(event.bytes!!,socket)
                res?.let{resHandler(it,socket.key)}
            }
            DISCONNECT->{
                pn.closeSocket(event.socket!!)
                pn.print_state()
            }
        }
    }

    private fun handleResolverEvent(event : ResolverEvent){
        when(event.type){
            CONNECT ->{
                pn.add(event.socket!!)
                pn.print_state()

                val user = event.socket.user
                val intent = Intent(netDef.code2String(CONNECT))
                intent.putExtra("extra", Json.encodeToString(user))
                context.sendBroadcast(intent)
            }
            PACKET ->{
                val socket = event.socket!!
                val res = op.receive(event.bytes!!,socket)
                res?.let{resHandler(it,socket.key)}
            }
            RETRY ->{
                suspendConnection(event.device!!.address)
            }
            DISCONNECT->{
                pn.closeSocket(event.socket!!)
                pn.print_state()

                val user = event.socket.user
                val intent = Intent(netDef.code2String(DISCONNECT))
                intent.putExtra("extra", Json.encodeToString(user))
                context.sendBroadcast(intent)
            }
        }

    }

    private fun resHandler(res : Int, key : String){
        when(res){
            MeshRaw.PING->{
                val intent = Intent("PING")
                intent.putExtra("extra", key)
                context.sendBroadcast(intent)
            }
            MeshRaw.DIRECT->{
                val intent = Intent("DIRECT")
                intent.putExtra("extra", key)
                context.sendBroadcast(intent)
            }
            MeshRaw.CONFIRM->{
                val intent = Intent("CONFIRM")
                intent.putExtra("extra", key)
                context.sendBroadcast(intent)
            }
        }
    }

    private fun updateMain(type : Int, obj : String){
        val intent = Intent(netDef.code2String(type))
        intent.putExtra("extra", obj)
        context.sendBroadcast(intent)
    }


}//NetLooper
