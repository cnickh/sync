package daemon.dev.field.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import daemon.dev.field.cereal.objects.Channel

@Dao
interface ChannelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Channel)

    @Update
    suspend fun update(item: Channel)

    @Query("SELECT contents FROM channel_table WHERE `name` = :name")
    suspend fun waitContents(name: String) : String

    @Query("SELECT contents FROM channel_table WHERE `name` = :name")
    fun getContents(name : String) : LiveData<String>

    @Query("UPDATE channel_table SET `contents`=:nwContents WHERE `name` = :name")
    fun updateContents(name : String, nwContents : String)

    @Query("SELECT name FROM channel_table")
    fun getChannels() : LiveData<List<String>>

    @Query("SELECT `key` FROM channel_table WHERE `name` = :name")
    suspend fun getKey(name : String) : String



}