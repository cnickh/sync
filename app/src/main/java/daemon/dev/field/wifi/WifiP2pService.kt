package daemon.dev.field.wifi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.net.wifi.p2p.*
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.net.wifi.p2p.nsd.WifiP2pUpnpServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pUpnpServiceRequest
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import daemon.dev.field.DEVICE_NAME
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.WIFI_UUID
//import daemon.dev.post.data.PostRAM
import daemon.dev.post.network.Client
import daemon.dev.post.network.Host
//import daemon.dev.post.network.P2pData


@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.O)
class WifiP2pService : Service(){

    companion object {
        const val TAG = "WifiP2p"
    }

    /**Lateinit vars*/
    private lateinit var manager : WifiP2pManager
    private lateinit var autoHandler : Handler
    private var channel: Channel? = null
    private var receiver: BroadcastReceiver? = null
    private var hostThread : Host? = null
    private var handler : Handler? = null

    /**Initialized vals*/
    private val intentFilter = IntentFilter()
    @RequiresApi(Build.VERSION_CODES.O)
    private val service: WifiP2pUpnpServiceInfo =
        WifiP2pUpnpServiceInfo.newInstance(WIFI_UUID.toString(), DEVICE_NAME, listOf("postID[$PUBLIC_KEY]"))
    private val request: WifiP2pUpnpServiceRequest = WifiP2pUpnpServiceRequest.newInstance()

    override fun onBind(p0: Intent?): IBinder? {
        return LocalBinder()
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG,"WifiP2p started")

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("TAG","Lack permission: ACCESS_FINE_LOCATION")
            return
        }

        if(!initP2p()){
            Log.e(TAG,"WifiP2p failed :(")
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d(TAG,"onStartCommand() called")

        receiver = P2pReceiver()
        registerReceiver(receiver, intentFilter)

        manager.removeGroup(channel,P2pRemoveGroupListener())

        //add a local service
        manager.clearLocalServices(channel,P2pClearLocalServiceListener())
        manager.addLocalService(channel, service, P2pAddServiceListener())

        //add a service search request
        manager.clearServiceRequests(channel,P2pClearServiceRequestListener())
        manager.addServiceRequest(channel,request,P2pServiceRequestListener())

        //Set service found call back
        manager.setUpnpServiceResponseListener(channel, DiscoveredP2pServiceCallback())

        //search for requested services
        manager.discoverServices(channel,P2pDiscoverServiceListener())

        return START_STICKY
    }

    fun reStartDiscovery(){
        Log.d(TAG,"calling reStartDiscovery")

        //add a service search request
        manager.clearServiceRequests(channel,P2pClearServiceRequestListener())
        manager.addServiceRequest(channel,request,P2pServiceRequestListener())

        //Set service found call back
        manager.setUpnpServiceResponseListener(channel, DiscoveredP2pServiceCallback())

        //search for requested services
        manager.discoverServices(channel,P2pDiscoverServiceListener())

        reMakeLocalService()
    }

    fun reMakeLocalService(){
        Log.d(TAG,"calling reMakeLocalService")

        //add a local service
        manager.clearLocalServices(channel,P2pClearLocalServiceListener())
        manager.addLocalService(channel, service, P2pAddServiceListener())
    }

    fun leaveGroup(){
        Log.d(TAG,"calling leaveGroup")
        manager.removeGroup(channel,P2pRemoveGroupListener())

//        if(P2pData.connected.value!!){
//            manager.removeGroup(channel,P2pRemoveGroupListener())
//            P2pData.connected.postValue(false)
//        }

    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG,"onDestroy() called")

        unregisterReceiver(receiver)
        manager.clearLocalServices(channel,P2pClearLocalServiceListener())
        manager.clearServiceRequests(channel,P2pClearServiceRequestListener())
        manager.removeGroup(channel,P2pRemoveGroupListener())

    }

    private fun initP2p(): Boolean {

        // Device capability definition check
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Log.e(TAG, "Wi-Fi Direct is not supported by this device.")
            return false
        }

        // Hardware capability check
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        if (wifiManager == null) {
            Log.e(TAG, "Cannot get Wi-Fi system service.")
            return false
        }

        if (!wifiManager.isP2pSupported) {
            Log.e(TAG, "Wi-Fi Direct is not supported by the hardware or Wi-Fi is off.")
            return false
        }

        manager = getSystemService(Context.WIFI_P2P_SERVICE)!! as WifiP2pManager

        channel = manager.initialize(this, mainLooper, null)
        if (channel == null) {
            Log.e(TAG, "Cannot initialize Wi-Fi Direct.")
            return false
        }

        //Launch autoconnect manages discovered peer sequentially
        val server = AutoConnect(manager, channel!!)
        server.start()

        //Get the handler for autoconnect
        autoHandler = server.getHandler()
        if(autoHandler == null){
            Log.e("P2p","Handler failed to initialize")
        }

        return true
    }

    /**WifiP2p receiver*/
    inner class P2pReceiver : BroadcastReceiver() {

        override fun onReceive(p0: Context?, p1: Intent?) {

            Log.d(TAG,"Broadcast Receiver called")

            when (p1?.action) {

                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    // Respond to new connection or disconnections
                    Log.d(TAG, "Connection status changed - ???")

                    val networkInfo : NetworkInfo? =
                        p1.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO) as NetworkInfo?

                    if(networkInfo!!.isConnected) {
                        Log.d(TAG, "NetworkInfo Connected")
                        manager.requestConnectionInfo(channel, ConnectionInfoListener())
                    }

                }

                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {

                    // Respond to this device's Wi-Fi state changing
                    val device : WifiP2pDevice? =
                        p1.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE) as WifiP2pDevice?

                    device?.let{
                        val status : String = when(it.status){
                            0->{"CONNECTED"}
                            1->{"INVITED"}
                            2->{"FAILED"}
                            3->{"AVAILABLE"}
                            4->{"UNAVAILABLE"}
                            else -> {"UNKNOWN"}
                        }
                        val addr = device.deviceAddress
                        val name = device.deviceName
                        Log.d(TAG, "$name : $addr Device status - $status")
                    }

                }

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    Log.d(TAG,"Peers changed :P")
                    manager.requestPeers(channel, PeerListener())
                }


            }

        }

        inner class PeerListener : WifiP2pManager.PeerListListener{
            override fun onPeersAvailable(p0: WifiP2pDeviceList?) {
                p0?.deviceList?.forEach { d ->

                    val name = d.deviceName
                    Log.d(TAG,"Peer: $name")

                }
            }
        }

        inner class ConnectionInfoListener : WifiP2pManager.ConnectionInfoListener {
            override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
                info?.let{ wifiInfo ->

                    Log.d(TAG,"Connection Info Available")

                    if(wifiInfo.isGroupOwner){
                        Log.d(TAG,"Am Group Owner")
                        //start host thread
                        if(hostThread == null){
                            hostThread = Host()
                            hostThread!!.start()
                        } else {

                        }


                    }else{
                        val address = wifiInfo.groupOwnerAddress
                        Log.d(TAG,"Am client Group Member owner @$address")

                        //start client thread
                        address?.let{
                            val clientThread = Client(it)
                            clientThread.start()
                        }
                    }

                }
                /**
                WifiP2pInfo Fields

                boolean groupFormed
                Indicates if a p2p group has been successfully formed

                InetAddress groupOwnerAddress
                Group owner address

                boolean isGroupOwner
                Indicates if the current device is the group owner

                */

            }

        }

        inner class GroupInfoListener : WifiP2pManager.GroupInfoListener {
            @RequiresApi(Build.VERSION_CODES.R)
            override fun onGroupInfoAvailable(group: WifiP2pGroup?) {

                group?.let {

                    Log.d(TAG,"Group Info Available")
                    //val freq = it.frequency
                    val intr = it.`interface`
                    val netid = it.networkId
                    val ssid = it.networkName
                    val pass = it.passphrase
                    val isown = it.isGroupOwner
                    Log.d(TAG,"intr[$intr] netid[$netid] " +
                            "ssid[$ssid] pass[$pass] isown[$isown]")

                }

                /**
                WifiP2pGroup methods

                Collection<WifiP2pDevice> 	getClientList()
                Get the list of clients currently part of the p2p group

                int getFrequency()
                Get the operating frequency (in MHz) of the p2p group

                String getInterface()
                Get the interface name on which the group is created

                int getNetworkId()
                The network ID of the P2P group in wpa_supplicant.

                String getNetworkName()
                Get the network name (SSID) of the group.

                WifiP2pDevice getOwner()
                Get the details of the group owner as a WifiP2pDevice object

                String 	getPassphrase()
                Get the passphrase of the group.

                boolean isGroupOwner()
                Check whether this device is the group owner of the created p2p group

                String	toString()
                Returns a string representation of the object.

                 */
            }

        }

    }

    /**Service discovery callback*/
    inner class DiscoveredP2pServiceCallback : WifiP2pManager.UpnpServiceResponseListener{
        override fun onUpnpServiceAvailable(p0: MutableList<String>?, p1: WifiP2pDevice?) {

            val name = p1!!.deviceName
            val address = p1.deviceAddress

            Log.d(TAG,"Service Discovered")
            Log.d(TAG,"I found $name @ $address")

            val result = AutoConnect.ScanResult(p0,p1)

            //call auto connect handler
            autoHandler.obtainMessage(
                0,0,0,result).sendToTarget()
        }
    }


    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): WifiP2pService = this@WifiP2pService
    }

}
