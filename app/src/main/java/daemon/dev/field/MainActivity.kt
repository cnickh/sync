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
import daemon.dev.field.cereal.objects.Comment
import daemon.dev.field.databinding.ActivityMainBinding
import daemon.dev.field.fragments.ChannelFragment
import daemon.dev.field.fragments.InboxFragment
import daemon.dev.field.fragments.ProfileFragment
import daemon.dev.field.fragments.model.*
import daemon.dev.field.nypt.Signature
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    lateinit var syncModel : SyncModel
    lateinit var resModel : ResourceModel
    lateinit var msgModel : MessengerModel


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(MAIN_TAG, "Main-thread["+Thread.currentThread().name +"]")

        val key = ByteArray(8)
        val random = SecureRandom()
        random.nextBytes(key)

        val signature = Signature()
        signature.init()
        PUBLIC_KEY = signature.getPublic()
        PRIVATE_KEY = signature.getPrivate()
        Log.v("Main","set key ${PUBLIC_KEY.toBase64()}")

        val syncModelFactory = SyncModelFactory(this)
        val resModelFactory = ResourceModelFactory(this)
        val msgModelFactory = MessengerModelFactory()

        syncModel = ViewModelProvider(this, syncModelFactory)[SyncModel::class.java]
        resModel = ViewModelProvider(this, resModelFactory)[ResourceModel::class.java]
        msgModel = ViewModelProvider(this, msgModelFactory)[MessengerModel::class.java]

        requestPermissions(
            arrayOf(LOCATION_FINE_PERM,CONNECTION_PERM),
            PERMISSION_REQUEST_LOCATION
        )

        createNotificationChannel()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.commit {
            replace<ProfileFragment>(R.id.fragment_view)
            addToBackStack(null)
        }

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

        msgModel.direct.observe(this, Observer {

            val msg = Json.decodeFromString<Comment>(it)

            if(msg.equals("d1sc0nn3ct")){
                msgModel.zeroSub(msg.key)
            }else {

                msgModel.createSub(msg.key)

                msgModel.receiveMessage(msg)

//                msgModel.printMsgMap()
//                val text = msg.comment
//                val duration = Toast.LENGTH_SHORT
//                val toast = Toast.makeText(this, text, duration)
//                toast.show()
            }
        })

    }
    private fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
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
                requestPermissions(
                    arrayOf(CONNECTION_PERM),
                    PERMISSION_REQUEST_CONNECTION
                )
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



}