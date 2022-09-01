package daemon.dev.field.network

import android.os.ConditionVariable
import android.os.Handler
import android.os.Looper
import android.os.Message

/**@brief this class loops continuously like the NetworkLooper but instead of receiving messages from
 * network callbacks the only outside messages are confirmation of ping. This is used to ensure
 * all active connections displayed by the UI are in fact still active. In the future this class could
 * provide insight into the connection speed. This class also implements a timeout for all connections
 * to allow for a more dynamic network.*/
class PingLoop : Thread() /*, Handler.Callback*/ {

    lateinit var mLooper : Looper
//    lateinit var mHandler : Handler

    override fun run() {
        Looper.prepare()
        mLooper = Looper.myLooper()!!
//        mHandler = Handler(mLooper,this)

        //loop over peers
            //send ping
            //wait on response for BLE_INTERVAL
            //check - disconnect


        Looper.loop()
    }

//    override fun handleMessage(msg: Message): Boolean {
//
//
//
//        return true
//    }

    fun kill(){
        mLooper.quit()
    }

}