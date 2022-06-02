package daemon.dev.field

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import daemon.dev.field.data.PostRAM
import daemon.dev.field.databinding.ActivityMainBinding
import daemon.dev.field.fragments.ChannelFragment
import daemon.dev.field.fragments.InboxFragment
import daemon.dev.field.fragments.ProfileFragment


class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PostRAM.static_init(this)

        PostRAM.createMyPost("post0","Whats good", listOf(),null)
        PostRAM.createMyPost("post1","Whats good", listOf(),null)


        requestPermissions(
            arrayOf(LOCATION_FINE_PERM),
            PERMISSION_REQUEST_LOCATION
        )

        createNotificationChannel()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navBar.setOnItemSelectedListener {

            when(it.itemId){

                R.id.home->{
                    supportFragmentManager.commit {
                        replace<InboxFragment>(R.id.fragment_view)
                        addToBackStack(null)
                    }
                }

                R.id.profile->{
                    supportFragmentManager.commit {
                        replace<ProfileFragment>(R.id.fragment_view)
                        addToBackStack(null)
                    }
                }

                R.id.channel->{
                    supportFragmentManager.commit {
                        replace<ChannelFragment>(R.id.fragment_view)
                        addToBackStack(null)
                    }
                }

            }

            return@setOnItemSelectedListener true
        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_LOCATION -> {
                Log.v(MAIN_TAG,"Checking MeshService")
            }
//            PERMISSION_REQUEST_STORAGE -> {
//
//                ImageRAM.loadThumbnails(this, 8, null)
//                requestPermissions(
//                    arrayOf(LOCATION_FINE_PERM),
//                    PERMISSION_REQUEST_LOCATION
//                )
//
//            }
            else -> Log.e(MAIN_TAG,"Permissions denied")
        }
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                "Mesh Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

}