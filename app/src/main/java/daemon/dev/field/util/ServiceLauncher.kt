package daemon.dev.field.util

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import daemon.dev.field.PROFILE_TAG
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.network.MeshService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class ServiceLauncher(val context : Context) {

    var mBluetoothLeService : MeshService? = null
    var bound = false

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBluetoothLeService = (service as MeshService.LocalBinder).getService()
            //mBluetoothLeService!!.start()
            Log.i("ServiceLauncher.kt" ,"mBluetoothLeService is bound")

            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBluetoothLeService = null
            Log.i("ServiceLauncher.kt" ,"mBluetoothLeService is unbound")

        }
    }
    fun send(user : String, meshRaw: MeshRaw){

        if (mBluetoothLeService == null){
            Log.i("ServiceLauncher.kt" ,"mBluetoothLeService is null (fuckaru - no - jutsu)")
        }

        mBluetoothLeService?.send(user,meshRaw)
    }
    fun checkStartMesh(profile : User): Boolean {

        val serviceClass = MeshService::class.java

        val manager = context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager

        var running = false

        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            Log.i("ServiceLauncher.kt" ,"(no - jutsu) (${service.service.className})")

            if (serviceClass.name == service.service.className) {
                running = true

                val mIntent = Intent(context, MeshService::class.java)
                mIntent.putExtra("ME", Json.encodeToString(profile))
               // bound = context.bindService(mIntent, mServiceConnection, BIND_AUTO_CREATE)
            }
        }

        if(!running){
            val mIntent = Intent(context, MeshService::class.java)
            mIntent.putExtra("ME", Json.encodeToString(profile))
            ContextCompat.startForegroundService(context, mIntent!!);
            bound = context.bindService(mIntent, mServiceConnection, BIND_AUTO_CREATE)
        }

        return running
    }


//    fun checkKillMesh() : Boolean{
//
//        mBluetoothLeService?. let {
//            val mIntent = Intent(context, MeshService::class.java)
//            context.stopService(mIntent)
//            //it.stopService(mIntent)
////            Log.i("no jutsu", "$bound")
////            it.unbindService(mServiceConnection)
//////          it.kill()
//            return true
//        } ?: run {
//            return false
//        }
//
//    }
    fun checkKillMesh() : Boolean{
        val serviceClass = MeshService::class.java

        val manager = context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager

        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                killService()
                return true
            }
        }

        return false
    }

    private fun killService(){
        if(bound){
            context.unbindService(mServiceConnection)
        }
        val mIntent = Intent(context, MeshService::class.java)
        context.stopService(mIntent)
    }
}