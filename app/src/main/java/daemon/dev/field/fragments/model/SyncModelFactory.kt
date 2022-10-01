package daemon.dev.field.fragments.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.data.ChannelAccess
import daemon.dev.field.data.PostRepository
import daemon.dev.field.data.UserBase
import daemon.dev.field.data.db.SyncDatabase
import kotlinx.coroutines.*
import kotlin.random.Random

class SyncModelFactory (private val context: Context) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncModel::class.java)) {

            val sync = SyncDatabase.getInstance(context)

            val postDao = sync.postDao
            val userDao = sync.userDao
            val channelDao = sync.channelDao

            CoroutineScope(Dispatchers.IO).launch {
                postDao.clear()
            }

            return SyncModel(PostRepository(postDao),
                UserBase(userDao),
                ChannelAccess(channelDao)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}