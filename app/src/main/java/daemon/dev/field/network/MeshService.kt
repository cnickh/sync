package daemon.dev.field.network

import android.R
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import daemon.dev.field.*
import daemon.dev.field.bluetooth.BluetoothAdvertiser
import daemon.dev.field.bluetooth.BluetoothScanner
import daemon.dev.field.bluetooth.Gatt
import kotlinx.coroutines.runBlocking


@RequiresApi(Build.VERSION_CODES.O)
class MeshService : Service() {

    /*Essentials for Operation*/
    val context : Context = this

    private lateinit var bluetoothManager : BluetoothManager
    private lateinit var adapter : BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothScanner
    private lateinit var bluetoothAdvertiser: BluetoothAdvertiser

    private lateinit var gatt : Gatt
    private lateinit var looper : NetworkLooper

    class NetworkSwitch(val bluetoothAdvertiser: BluetoothAdvertiser, val scanner : BluetoothScanner){

        fun on(){
            bluetoothAdvertiser.startAdvertisement()
            scanner.startScanning()
        }

        fun off(){
            bluetoothAdvertiser.stopAdvertising()
            scanner.stopScanning()
        }


    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        bluetoothManager = this.application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = bluetoothManager.adapter

        Log.i(MESH_TAG,"Created successfully")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(MESH_TAG,"Started successfully")
        Log.d(MESH_TAG, "MeshService-thread["+Thread.currentThread().name +"]")

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

        bluetoothAdvertiser = BluetoothAdvertiser(adapter)
        bluetoothLeScanner = BluetoothScanner(adapter, looper.getHandler())
        val switch = NetworkSwitch(bluetoothAdvertiser,bluetoothLeScanner)

        runBlocking{ Async.ready(context,looper.getHandler(),switch) }

        gatt = Gatt(this.application, bluetoothManager, adapter, looper.getHandler())
        gatt.start()

        switch.on()

    }

}