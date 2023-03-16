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
import daemon.dev.field.data.UserBase
import daemon.dev.field.data.db.SyncDatabase
import kotlinx.coroutines.*
import java.util.*
import kotlin.random.Random

class SyncModelFactory (private val context: Context) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncModel::class.java)) {

            val sync = SyncDatabase.getInstance(context)

            val postDao = sync.postDao
            val userDao = sync.userDao
            val channelDao = sync.channelDao

            CoroutineScope(Dispatchers.IO).launch {

                userDao.clear()
                postDao.clear()
                channelDao.clear()

                channelDao.insert(Channel("Public", UNIVERSAL_KEY, "null"))
                Log.i("Fac-Debug" ,"What did we just insert?? ${channelDao.getKey("Public")}")

                channelDao.insert(Channel("test", "null", "null"))

                val channels = channelDao.waitChannels()
                Log.i("Facz-Debug" ,"What did we just insert?? $channels")

                if(userDao.wait(PUBLIC_KEY.toBase64()) == null){
                    val num = Random.nextInt(999)
                    val user = User(PUBLIC_KEY.toBase64(),"anon#$num",0, "null")
                    userDao.insert(user)
                    Log.v("Main", "${userDao.wait(PUBLIC_KEY.toBase64())} inserted")
                }else{
                    Log.v("Main","user already exists")
                }

            }

            return SyncModel(PostRepository(postDao),
                UserBase(userDao),
                ChannelAccess(channelDao)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    private fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
    }
}