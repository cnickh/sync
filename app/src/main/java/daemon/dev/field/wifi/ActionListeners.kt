package daemon.dev.field.wifi

import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

const val TAG = "P2pActionListener"

/**Action listeners *simple (failed/success) */
class P2pConnectListener : WifiP2pManager.ActionListener {
    override fun onSuccess() {

        Log.d(TAG,"Initiate connection Successful")

    }

    override fun onFailure(p0: Int) {

        Log.d(TAG,"Initiate connection failed with code $p0")

    }

}
class P2pCancelConnectListener : WifiP2pManager.ActionListener {
    override fun onSuccess() {

        Log.d(TAG,"Cancel out-going connections Successful")

    }

    override fun onFailure(p0: Int) {

        Log.d(TAG,"Cancel out-going connections failed with code $p0")

    }

}
class P2pAddServiceListener : WifiP2pManager.ActionListener {
    override fun onSuccess() {

        Log.d(TAG,"P2p service added successfully")

    }

    override fun onFailure(p0: Int) {

        Log.d(TAG,"P2p service failed to add with code $p0")

    }

}
class P2pServiceRequestListener : WifiP2pManager.ActionListener {
    override fun onSuccess() {

        Log.d(TAG,"P2p service requested successfully")

    }

    override fun onFailure(p0: Int) {

        Log.d(TAG,"P2p service failed to be requested with code $p0")

    }

}
class P2pDiscoverServiceListener : WifiP2pManager.ActionListener {
    override fun onSuccess() {

        Log.d(TAG,"P2p service discovery started successfully")

    }

    override fun onFailure(p0: Int) {

        Log.d(TAG,"P2p service discover failed to start with code $p0")

    }

}
class P2pClearLocalServiceListener : WifiP2pManager.ActionListener {
    override fun onSuccess() {

        Log.d(TAG,"P2p service cleared successfully")

    }

    override fun onFailure(p0: Int) {

        Log.d(TAG,"P2p service clear failed code $p0")

    }

}
class P2pClearServiceRequestListener : WifiP2pManager.ActionListener {
    override fun onSuccess() {

        Log.d(TAG,"P2p service requests removed successfully")

    }

    override fun onFailure(p0: Int) {

        Log.d(TAG,"P2p service requests not removed with code $p0")

    }

}
class P2pRemoveGroupListener : WifiP2pManager.ActionListener {
    override fun onSuccess() {
        Log.d(TAG,"P2p group removed successfully")
    }

    override fun onFailure(p0: Int) {
        Log.d(TAG,"P2p group failed to remove with code $p0")
    }
}
class P2pGroupListener : WifiP2pManager.ActionListener {
    override fun onSuccess() {
        Log.d(TAG,"P2p group create successfully")
    }

    override fun onFailure(p0: Int) {
        Log.d(TAG,"P2p group failed with code $p0")
    }
}