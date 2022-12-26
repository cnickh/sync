package daemon.dev.field.wifi

import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import daemon.dev.field.WIFI_UUID
//import daemon.dev.post.network.P2pData
//import daemon.dev.post.network.P2pData.cLock

class AutoConnect(val manager : WifiP2pManager, val channel : WifiP2pManager.Channel) : Thread(), Handler.Callback {

    class ScanResult(val p0: MutableList<String>?, val p1: WifiP2pDevice?)

    val AUTO_TAG = "AutoConnect.kt"

    var mHandler: Handler? = null

    fun getHandler(): Handler {

        while (mHandler == null) {
        }

        return mHandler!!
    }

    override fun run() {
        Looper.prepare()
        mHandler = Handler(Looper.myLooper()!!, this)
        Looper.loop()
    }

    override fun handleMessage(msg: Message): Boolean {

        Log.d(AUTO_TAG,"Handling discovered service")

        val result = msg.obj as ScanResult
        val address = result.p1!!.deviceAddress

        Log.d(AUTO_TAG,"I have @$address")


        result.p0?.let{
            Log.d(AUTO_TAG,"Have service list: $it")

            for (s in it) {
                val uuid = s.split(':')[1]
                Log.d(AUTO_TAG,"Service $uuid is available")
                if(uuid == WIFI_UUID.toString()){

                    val config = WifiP2pConfig()
                    config.deviceAddress = address

                    Log.d(AUTO_TAG, "Calling connect...")
                    manager.connect(channel!!, config, P2pConnectListener())
//                    synchronized(cLock) {
//                        if (!P2pData.connected.value!!) {
//
//                            cLock.wait()
//                        }
//                    }
                } else {
                    Log.d(AUTO_TAG,"Already connected")
                }

            }
        }

        return true
    }

}