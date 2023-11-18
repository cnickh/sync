package daemon.dev.field

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.badge.BadgeDrawable
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.databinding.ActivityMainBinding
import daemon.dev.field.fragments.ChannelFragment
import daemon.dev.field.fragments.InboxFragment
import daemon.dev.field.fragments.ProfileFragment
import daemon.dev.field.fragments.model.*
import daemon.dev.field.nypt.Signature
import daemon.dev.field.util.KeyStore
import daemon.dev.field.util.ServiceLauncher
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    lateinit var syncModel : SyncModel
    lateinit var resModel : ResourceModel
    lateinit var msgModel : MessengerModel

    val mGattUpdateReceiver = MyReceiver()
    private val mServiceController = ServiceLauncher(this)

    var msg : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            this.supportActionBar!!.hide()
        } catch (_: NullPointerException) {

        }

        Log.d(MAIN_TAG, "Main-thread["+Thread.currentThread().name +"]")

        val key = ByteArray(8)
        val random = SecureRandom()
        random.nextBytes(key)

        val signature = Signature()
        signature.init()

        val keyStore = KeyStore(this)

        if(keyStore.checkKey()){
            Log.d(MAIN_TAG,"Key retrieved")
            PUBLIC_KEY = keyStore.publicKey()!!
            PRIVATE_KEY = keyStore.privateKey()!!
        }else{
            Log.d(MAIN_TAG,"No key found")
            if(PUBLIC_KEY.contentEquals(ByteArray(0))){
                PUBLIC_KEY = signature.getPublic()
            }
            if(PRIVATE_KEY.contentEquals(ByteArray(0))){
                PRIVATE_KEY = signature.getPrivate()
            }

            keyStore.storeKeys()
        }

        Log.v("Main","set key ${PUBLIC_KEY.toBase64()}")
        Log.v("Main","set key ${PRIVATE_KEY.toBase64()}")

        val syncModelFactory = SyncModelFactory(this)
        val resModelFactory = ResourceModelFactory(this)
        val msgModelFactory = MessengerModelFactory()

        syncModel = ViewModelProvider(this, syncModelFactory)[SyncModel::class.java]
        resModel = ViewModelProvider(this, resModelFactory)[ResourceModel::class.java]
        msgModel = ViewModelProvider(this, msgModelFactory)[MessengerModel::class.java]

        syncModel.setServiceController(mServiceController)

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

        syncModel.channels.observe(this) {
    
            var badge: BadgeDrawable? = null
            
            for (c in it){
                if(c.key.split(":")[0] == "shared"){
                    badge = binding.navBar.getOrCreateBadge(R.id.channel)
                    badge.isVisible = true
                }
            }
            
            if(badge == null){
                badge = binding.navBar.getOrCreateBadge(R.id.channel)
                badge.isVisible = false
            }

        }

//        msgModel.direct.observe(this) {
//            Log.d(MAIN_TAG, "Main-thread direct Observer")
//
//            val msg = Json.decodeFromString<Comment>(it)
//
//            if (msg.comment == "d1sc0nn3ct") {
//                msgModel.zeroSub(msg.key)
//            } else {
//
//                msgModel.createSub(msg.key)
//                msgModel.receiveMessage(msg)
//                msgModel.setLatest(msg)
//
//                val text = msg.comment
//                val duration = Toast.LENGTH_SHORT
//                val toast = Toast.makeText(this, text, duration)
//                toast.show()
//            }
//            msgModel.printMsgMap()
//        }
//
//        syncModel.peers.observe(this) { peers ->
//            Log.d(MAIN_TAG, "Main-thread peers Observer")
//
//            for (p in peers) {
//                msgModel.dumpQueue(p.key)
//            }
//            msgModel.printMsgMap()
//        }

    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
    }
    private fun makeGattUpdateIntentFilter(): IntentFilter? {
        val intentFilter = IntentFilter()
        intentFilter.addAction("STATE")
        intentFilter.addAction("SCANNER")
        intentFilter.addAction("CONNECT")
        intentFilter.addAction("DISCONNECT" )
        intentFilter.addAction("PING" )

        return intentFilter
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

    inner class MyReceiver: BroadcastReceiver() {

        val peers = mutableListOf<User>()
        val devices = mutableListOf<String>()
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {

            val it = intent.extras!!.getString("extra")!!

            when(intent.action){
                "STATE" ->{
                    //String state
                    syncModel.state.postValue(it)
                }
                "SCANNER" ->{
                    //String device address
                    devices.add(it)
                    syncModel.devices.postValue(devices)
                }
                "CONNECT" ->{
                    //User object
                    val peer = Json.decodeFromString<User>(it)
                    peers.add(peer)
                    syncModel.peers.postValue(peers)
                }
                "DISCONNECT" ->{
                    //User object
                    val peer = Json.decodeFromString<User>(it)
                    peers.remove(peer)
                    syncModel.peers.postValue(peers)
                }
                "PING" ->{
                    binding.byteRate.text = "${it.slice(0..4)} -- ${msg++}"
                }

            }

        }
    }


}