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
import daemon.dev.field.data.Resolver
import daemon.dev.field.data.Server
import kotlinx.coroutines.runBlocking


@RequiresApi(Build.VERSION_CODES.O)
class MeshService : Service() {

    /*Essentials for Operation*/
    val context : Context = this

    private lateinit var bluetoothLeScanner: BluetoothScanner
    private lateinit var gatt : Gatt
    private lateinit var server: Server
    private lateinit var resolver: Resolver

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(MESH_TAG,"Created successfully")
        bluetoothLeScanner = BluetoothScanner(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(MESH_TAG,"Started successfully")

        enableNotification()

        launchNetworkProcesses()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothLeScanner.stopScanning()
        runBlocking{PeerRAM.disconnectAll()}
        gatt.stopServer()
        PeerRAM.setGatt(null)
        PeerRAM.setResolver(null)
        PeerRAM.setServer(null)
        resolver.kill()
        server.kill()

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

        if (PeerRAM.isServer()) {
            Log.v(MESH_TAG,"Starting Server")
            server = Server()
            server.start()
            PeerRAM.setServer(server.getHandler())
        }

        if (PeerRAM.isResolver()) {
            Log.v(MESH_TAG,"Starting Resolver")
            resolver = Resolver(server.getHandler())
            resolver.start()
            PeerRAM.setResolver(resolver.getHandler())
        }

        if (PeerRAM.isGatt()) {
            gatt = Gatt(this.application, resolver.getHandler())
            gatt.start()
            while (gatt.gattServer == null) {}
            PeerRAM.setGatt(gatt.gattServer!!)
        }

        bluetoothLeScanner.startScanning()

    }

}