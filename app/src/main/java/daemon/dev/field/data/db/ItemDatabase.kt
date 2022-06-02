package daemon.dev.field.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PostEntity::class,UserEntity::class], version = 1, exportSchema = false)
internal abstract class ItemDatabase : RoomDatabase() {

    abstract val itemDatabaseDao: ItemDatabaseDao
    companion object {

        @Volatile
        private var INSTANCE: ItemDatabase? = null

        fun getInstance(context: Context): ItemDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ItemDatabase::class.java,
                        "post_database"
                    )
                        .fallbackToDestructiveMigration()
//                        .allowMainThreadQueries()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }


}