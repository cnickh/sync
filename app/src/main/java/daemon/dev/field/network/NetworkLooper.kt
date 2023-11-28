package daemon.dev.field.network

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice.*
import android.content.Context
import android.content.Intent
import android.os.ConditionVariable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.ParcelUuid
import android.util.Log
import androidx.compose.material.contentColorFor
import daemon.dev.field.*
import daemon.dev.field.bluetooth.Gatt
import daemon.dev.field.bluetooth.GattResolver
import daemon.dev.field.cereal.objects.HandShake
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.Post
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
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

    private var mHandler: Handler? = null
    private var mLooper: Looper? = null

    private val scannedDevices = mutableListOf<String>()

    private lateinit var switch : MeshService.NetworkSwitch
    private val op = SyncOperator(context)
    private val pn = PeerNetwork()

    private var gattConnectionCount = 0
    private var deviceConnectionCount = 0

    private lateinit var gatt : Gatt
    private val netDef = NetworkEventDefinition()

    private val connection = ConditionVariable(true)
    private val handlerMutex = Mutex()



    fun setGatt(gatt : Gatt){
        this.gatt = gatt
    }

    fun setSwitch(switch : MeshService.NetworkSwitch){
        this.switch = switch
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
        sendDisconnects()
        pn.clear()
        updateMain(STATE,"IDLE")
    }

    private fun sendDisconnects(){

        val raw = MeshRaw(MeshRaw.DISCONNECT,null,null,null,null,null)

        for (p in pn.peers()){
            pn.gattConnection(p)?.let{
                val packer = Packer(raw)
                it.send(packer)==-1
            }
        }

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

                closeSocket(it)

                val user = it.user
                val intent = Intent(netDef.code2String(DISCONNECT))
                intent.putExtra("extra", Json.encodeToString(user))
                context.sendBroadcast(intent)
            }
        }
    }


    private fun handleScanEvent(event : ScanEvent){

        val adKey = event.result.scanRecord?.getServiceData(ParcelUuid(SERVICE_UUID))

        adKey?.let {

            CoroutineScope(Dispatchers.IO).launch{
                //Log.i(NETLOOPER_TAG, "Waiting on connection")
                handlerMutex.lock()
                if(!connection.block(2*CONFIRMATION_TIMEOUT)){
                    handlerMutex.unlock()
                    return@launch
                }
                Log.d(NETLOOPER_TAG, "Blocking on connection")


                val key = it.toBase64()
                //val devRes = getDevice(key)
                val pnRes = pn.containsAd(key)
                val state = gattConnectionCount < MAX_PEERS

                Log.i(NETLOOPER_TAG," pnRes: $pnRes state: $state")
                Log.i(NETLOOPER_TAG,"scannedDevices: $scannedDevices")

                if (pnRes && state) {
                    connection.close()

                    Log.i(NETLOOPER_TAG, "Have ${it.toBase64()}")
                    Log.i(NETLOOPER_TAG, "Connecting scanning ${event.result.device.address}")
                    Log.i(NETLOOPER_TAG, "scannedDevices: $scannedDevices")
                    pn.print_state()

                    val gattCallback =
                        GattResolver(
                            event.result.device,
                            getHandler(),
                            HandShake(0, profile, null, null),
                            it.toBase64()
                        )

                    event.result.device.connectGatt(
                        context,
                        false,
                        gattCallback,
                        TRANSPORT_LE,
                        PHY_LE_CODED
                    )
                    updateMain(SCANNER, it.toBase64())
                }

                handlerMutex.unlock()
            }
        }

    }


    private fun handleGattEvent(event : GattEvent){

        when(event.type){
            CONNECT ->{
                add(event.socket!!)
                pn.print_state()
            }
            PACKET ->{
                val socket = event.socket!!
                val res = op.receive(event.bytes!!,socket)
                res?.let{resHandler(it,socket)}
            }
            DISCONNECT->{
                closeSocket(event.socket!!)
                pn.print_state()
            }
        }
    }

    private fun handleResolverEvent(event : ResolverEvent){
        when(event.type){
            CONNECT ->{
                add(event.socket!!)
                pn.print_state()

                connection.open()
                val user = event.socket.user
                val intent = Intent(netDef.code2String(CONNECT))
                intent.putExtra("extra", Json.encodeToString(user))
                context.sendBroadcast(intent)
            }
            PACKET ->{
                val socket = event.socket!!
                val res = op.receive(event.bytes!!,socket)
                res?.let{resHandler(it,socket)}
            }
            RETRY ->{
                //removeDev(event.bytes!!.toBase64())

                connection.open()

                val intent = Intent("RM_DEVICE")
                intent.putExtra("extra", event.bytes!!.toBase64())
                context.sendBroadcast(intent)

               // suspendConnection(event.bytes.toBase64())
            }
            DISCONNECT->{
                closeSocket(event.socket!!)
                pn.print_state()

                val user = event.socket.user
                val intent = Intent(netDef.code2String(DISCONNECT))
                intent.putExtra("extra", Json.encodeToString(user))
                context.sendBroadcast(intent)
            }
        }

    }

    private fun resHandler(res : Int, sock : Socket){
        when(res){
            MeshRaw.PING->{
                val intent = Intent("PING")
                intent.putExtra("extra", sock.key)
                context.sendBroadcast(intent)
            }
            MeshRaw.DIRECT->{
                val intent = Intent("DIRECT")
                intent.putExtra("extra", sock.key)
                context.sendBroadcast(intent)
            }
            MeshRaw.CONFIRM->{
                val intent = Intent("CONFIRM")
                intent.putExtra("extra", sock.key)
                context.sendBroadcast(intent)
            }
            MeshRaw.DISCONNECT->{
                closeSocket(sock)
                pn.gattConnection(sock.key)?.let{
                    closeSocket(it)
                }
                val intent = Intent("DISCONNECT")
                intent.putExtra("extra", Json.encodeToString(sock.user))
                context.sendBroadcast(intent)
            }
        }
    }

    private fun updateMain(type : Int, obj : String){
        val intent = Intent(netDef.code2String(type))
        intent.putExtra("extra", obj)
        context.sendBroadcast(intent)
    }

    private fun closeSocket(socket : Socket){

        val tuple = pn.closeSocket(socket)
        gattConnectionCount = tuple.first
        deviceConnectionCount = tuple.second

        if(gattConnectionCount < MAX_PEERS){
            switch.startScanning()
            val intent = Intent("STATE")
            intent.putExtra("extra", "READY")
            context.sendBroadcast(intent)
        }

        if(deviceConnectionCount < MAX_PEERS+2){
            switch.startAdvertising()
        }

    }

    private fun add(socket : Socket){
        val tuple = pn.add(socket)
        gattConnectionCount = tuple.first
        deviceConnectionCount = tuple.second

        if(gattConnectionCount == MAX_PEERS){
            switch.stopScanning()
            val intent = Intent("STATE")
            intent.putExtra("extra", "INSYNC")
            context.sendBroadcast(intent)
        }

        if(deviceConnectionCount == MAX_PEERS+2){
            switch.stopAdvertising()
        }

    }
}//NetLooper
