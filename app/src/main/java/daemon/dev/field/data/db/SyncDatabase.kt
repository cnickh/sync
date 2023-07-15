package daemon.dev.field.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import daemon.dev.field.cereal.objects.Channel
import daemon.dev.field.cereal.objects.Post
import daemon.dev.field.cereal.objects.Resource
import daemon.dev.field.cereal.objects.User

@Database(entities =
[Post::class, User::class, Channel::class, Resource::class],
    version = 2)
internal abstract class SyncDatabase : RoomDatabase() {

    abstract val postDao: PostDao
    abstract val userDao : UserDao
    abstract val channelDao : ChannelDao
    abstract val resDao : ResDao

    companion object {

        @Volatile
        private var INSTANCE: SyncDatabase? = null

        fun getInstance(context: Context): SyncDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SyncDatabase::class.java,
                        "sync_database"
                    )
                        .fallbackToDestructiveMigration()
//                        .allowMainThreadQueries()
                        .build()
                    INSTANCE = instance
                }

                return instance
            }
        }

        fun destroyInstance() {
            if (INSTANCE != null) INSTANCE!!.close()
            INSTANCE = null
        }

    }


}