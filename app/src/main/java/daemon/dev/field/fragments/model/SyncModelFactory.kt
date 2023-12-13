package daemon.dev.field.fragments.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import daemon.dev.field.CHARSET
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.UNIVERSAL_KEY
import daemon.dev.field.cereal.objects.Channel
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.data.ChannelAccess
import daemon.dev.field.data.PostRepository
import daemon.dev.field.data.SyncInterface
import daemon.dev.field.data.UserBase
import daemon.dev.field.data.db.SyncDatabase
import kotlinx.coroutines.*
import java.util.*
import kotlin.random.Random

class SyncModelFactory (private val context: Context) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        val pub_key = PUBLIC_KEY.toBase64()

        if (modelClass.isAssignableFrom(SyncModel::class.java)) {

            val sync = SyncDatabase.getInstance(context)

            val postDao = sync.postDao
            val userDao = sync.userDao
            val channelDao = sync.channelDao

            CoroutineScope(Dispatchers.IO).launch {

                val pub = channelDao.getKey("Public")

                if(pub != UNIVERSAL_KEY){
                    channelDao.insert(Channel("Public", UNIVERSAL_KEY))
                }

                if(userDao.wait(pub_key) == null){
                    val num = Random.nextInt(999)
                    Log.i("Facz-Debug" ,"We generated:$num")
                    val user = User(pub_key,"anon#$num",0, "null")
                    userDao.insert(user)
                    Log.v("Main", "${userDao.wait(pub_key)} inserted")
                }else{
                    Log.v("Main","user already exists")
                }

            }

            return SyncModel(
                SyncInterface(postDao,channelDao,userDao)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    private fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
    }
}