package daemon.dev.field.network

import android.R
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import daemon.dev.field.*
import daemon.dev.field.bluetooth.BluetoothScanner
import daemon.dev.field.bluetooth.Gatt
import kotlinx.coroutines.runBlocking


@RequiresApi(Build.VERSION_CODES.O)
class MeshService : Service() {

    /*Essentials for Operation*/
    val context : Context = this

    private lateinit var bluetoothLeScanner: BluetoothScanner
    private lateinit var gatt : Gatt
    private lateinit var looper : NetworkLooper

    class NetworkSwitch(val gatt: Gatt, val scanner : BluetoothScanner){

        fun on(){
            gatt.startAdvertising()
            scanner.startScanning()
        }

        fun off(){
            gatt.stopAdvertising()
            scanner.stopScanning()
        }


    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(MESH_TAG,"Created successfully")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(MESH_TAG,"Started successfully")

        enableNotification()
        launchNetworkProcesses()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        runBlocking{
            Async.idle()
        }

        bluetoothLeScanner.stopScanning()
        gatt.stopServer()
        looper.kill()
        Log.i(MESH_TAG,"Killed successfully")
    }

    private fun enableNotification(){

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
            0, notificationIntent, 0)

        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setContentTitle("Mesh Service")
            .setContentText("connecting...")
            .setSmallIcon(R.mipmap.sym_def_app_icon)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun launchNetworkProcesses(){
        Log.d(MESH_TAG,"launchNetworkProcesses() called")

        looper = NetworkLooper(this)
        looper.start()

        gatt = Gatt(this.application,looper.getHandler())
        gatt.start()

        bluetoothLeScanner = BluetoothScanner(this, looper.getHandler())
        bluetoothLeScanner.startScanning()

        val switch = NetworkSwitch(gatt,bluetoothLeScanner)

        looper.addSwitch(switch)

        runBlocking{ Async.ready(context,looper.getHandler(),switch) }

    }

}