package daemon.dev.field.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import daemon.dev.field.network.MeshService

@RequiresApi(Build.VERSION_CODES.O)
class ServiceLauncher(val context : Context) {

    fun checkStartMesh(): Boolean {

        val serviceClass = MeshService::class.java

        val manager = context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager

        var running : Boolean = false

        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                running = true
            }
        }

        if(!running){
            startService()
        }

        return running
    }

    private fun startService(){
        val mintent = Intent(context, MeshService::class.java)
        ContextCompat.startForegroundService(context, mintent);
    }

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
        val mintent = Intent(context, MeshService::class.java)
        context.stopService(mintent)
    }

}