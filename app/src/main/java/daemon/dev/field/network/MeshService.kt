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
    private lateinit var switch : NetworkSwitch

    private lateinit var gatt : Gatt
    private lateinit var looper : NetworkLooper

    private lateinit var me : User

    class NetworkSwitch(val bluetoothAdvertiser: BluetoothAdvertiser, val scanner : BluetoothScanner){

        var scanState = false
        var adState = false

        fun on(){
            scanState = true
            adState = true
            bluetoothAdvertiser.startAdvertisement()
            scanner.startScanning()
        }

        fun off(){
            scanState = false
            adState = false
            bluetoothAdvertiser.stopAdvertising()
            scanner.stopScanning()
        }

        fun stopScanning(){
            if(scanState){
                scanState = false
                scanner.stopScanning()
            }
        }

        fun startScanning(){
            if(!scanState){
                scanState = true
                scanner.startScanning()
            }
        }

        fun stopAdvertising(){
            if(adState){
                adState = false
                bluetoothAdvertiser.stopAdvertising()
            }
        }

        fun startAdvertising(){
            if(!adState){
                adState = true
                bluetoothAdvertiser.startAdvertisement()
            }
        }

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

        val me = intent?.extras?.getString("ME")
        if (me != null) {
            this.me = Json.decodeFromString<User>(me)
        }//get shake
        start()

        return START_NOT_STICKY
    }

    fun start(){
        enableNotification()
        launchNetworkProcesses()
    }

    fun kill(){
        switch.off()
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

    fun notify(key : String){
        looper.getHandler().obtainMessage(
            NetworkEventDefinition.APP,
            NetworkEventDefinition.AppEvent(key,null,)
        ).sendToTarget()
    }

    private fun launchNetworkProcesses(){
        Log.d(MESH_TAG,"launchNetworkProcesses() called")

        this.sendBroadcast(Intent(""))
        looper = NetworkLooper(this@MeshService,me)
        looper.start()

        bluetoothAdvertiser = BluetoothAdvertiser(adapter)
        bluetoothLeScanner = BluetoothScanner(adapter, looper.getHandler())
        switch = NetworkSwitch(bluetoothAdvertiser,bluetoothLeScanner)

        gatt = Gatt(this.application, bluetoothManager, adapter, looper.getHandler(), HandShake(0,me,null,null))

        looper.setGatt(gatt)
        looper.setSwitch(switch)

        gatt.start()

        switch.on()

    }

}