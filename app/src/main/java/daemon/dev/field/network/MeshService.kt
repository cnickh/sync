package daemon.dev.field.network

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import daemon.dev.field.MESH_TAG
import daemon.dev.field.MainActivity
import daemon.dev.field.NOTIFICATION_CHANNEL
import daemon.dev.field.NOTIFICATION_ID
import daemon.dev.field.bluetooth.BluetoothAdvertiser
import daemon.dev.field.bluetooth.BluetoothScanner
import daemon.dev.field.bluetooth.Gatt
import daemon.dev.field.cereal.objects.HandShake
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.network.util.NetworkEventDefinition
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class MeshService : Service() {

    /*Essentials for Operation*/
    val context : Context = this

    private lateinit var bluetoothManager : BluetoothManager
    private lateinit var adapter : BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothScanner
    private lateinit var bluetoothAdvertiser: BluetoothAdvertiser

    private lateinit var gatt : Gatt
    private lateinit var looper : NetworkLooper

    private lateinit var me : User

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
//    class LocalBinder : Binder() {
//        val service: MeshService
//            get() = this@MeshService
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return mBinder
//    }
//
//    override fun onUnbind(intent: Intent?): Boolean {
//        // After using a given device, you should make sure that BluetoothGatt.close() is called
//        // such that resources are cleaned up properly.  In this particular example, close() is
//        // invoked when the UI is disconnected from the Service.
//        close()
//        return super.onUnbind(intent)
//    }
//
//    private val mBinder: IBinder = LocalBinder()

    override fun onCreate() {
        super.onCreate()

        bluetoothManager = this.application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = bluetoothManager.adapter

        Log.i(MESH_TAG,"Created successfully")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(MESH_TAG,"Started successfully")
        Log.d(MESH_TAG, "MeshService-thread["+Thread.currentThread().name +"]")

        val me = intent?.extras?.getString("ME")
        if (me != null) {
            this.me = Json.decodeFromString<User>(me)
        }//get shake
        start()
//        enableNotification()
//        launchNetworkProcesses()

        return START_NOT_STICKY
    }

    fun start(){
        enableNotification()
        launchNetworkProcesses()
    }

    fun kill(){
        bluetoothLeScanner.stopScanning()
        bluetoothAdvertiser.stopAdvertising()
        gatt.stopServer()
        looper.kill()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        Log.i(MESH_TAG,"Killed successfully")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(MESH_TAG,"onDestroy() called")

        kill()
    }

    private val mBinder: IBinder = LocalBinder()

    override fun onBind(p0: Intent?): IBinder {
        val me = p0?.extras?.getString("ME")
        if (me != null) {
            this.me = Json.decodeFromString<User>(me)
        }//get shake
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): MeshService = this@MeshService
    }

    fun selectChannel(){

    }

    private fun enableNotification(){

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setContentTitle("Mesh Service")
            .setContentText("connecting...")
            .setSmallIcon(android.R.mipmap.sym_def_app_icon)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    fun send(user : String, meshRaw: MeshRaw){
        looper.getHandler().obtainMessage(
            NetworkEventDefinition.APP,
            NetworkEventDefinition.AppEvent(user,meshRaw)
        ).sendToTarget()
    }

    private fun launchNetworkProcesses(){
        Log.d(MESH_TAG,"launchNetworkProcesses() called")

        this.sendBroadcast(Intent(""))

        looper = NetworkLooper(this@MeshService,me)
        looper.start()

        gatt = Gatt(this.application, bluetoothManager, adapter, looper.getHandler(), HandShake(0,me,null,null))

        looper.setGatt(gatt)

        bluetoothAdvertiser = BluetoothAdvertiser(adapter)
        bluetoothLeScanner = BluetoothScanner(adapter, looper.getHandler())
        val switch = NetworkSwitch(bluetoothAdvertiser,bluetoothLeScanner)

        gatt.start()

        switch.on()

    }

}