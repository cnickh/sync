package daemon.dev.field

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.data.db.SyncDatabase
import daemon.dev.field.databinding.ActivityMainBinding
import daemon.dev.field.fragments.ChannelFragment
import daemon.dev.field.fragments.InboxFragment
import daemon.dev.field.fragments.ProfileFragment
import daemon.dev.field.fragments.model.SyncModel
import daemon.dev.field.fragments.model.SyncModelFactory
import daemon.dev.field.nypt.KEY_TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.SecureRandom
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    lateinit var syncModel : SyncModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val key = ByteArray(8)
        val random = SecureRandom()
        random.nextBytes(key)

        PUBLIC_KEY = key.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
        Log.v("Main","set key $PUBLIC_KEY")

        val viewModelFactory = SyncModelFactory(this)
        syncModel = ViewModelProvider(this, viewModelFactory)[SyncModel::class.java]

        check_init()

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

        syncModel.ping.observe(this, Observer {
            val text = "Ping from $it"
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(this, text, duration)
            toast.show()
        })

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
            else -> Log.e(MAIN_TAG,"Permissions denied")
        }
    }

    private fun createNotificationChannel() {

        val serviceChannel = NotificationChannel(
            NOTIFICATION_CHANNEL,
            "Mesh Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(
            NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun check_init(){
        val userDao = SyncDatabase.getInstance(this).userDao

        CoroutineScope(Dispatchers.IO).launch {
            userDao.clear()
            if(userDao.wait(PUBLIC_KEY) == null){
                val num = Random.nextInt(999)
                val user = User(PUBLIC_KEY,"anon#$num",0, listOf())
                userDao.insert(user)
                Log.v("Main", "${userDao.wait(PUBLIC_KEY)} inserted")
            }else{
                Log.v("Main","user already exists")
            }
        }
    }

}