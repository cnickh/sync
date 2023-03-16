package daemon.dev.field.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import daemon.dev.field.cereal.objects.Channel

@Dao
interface ChannelDao {

    @Query("DELETE FROM channel_table")
    fun clear()

    @Query("DELETE FROM channel_table WHERE `name` = :name")
    suspend fun delete(name : String)

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

    @Query("UPDATE channel_table SET `key`=:key WHERE `name` = :name")
    suspend fun updateKey(name : String, key : String)

    @Query("SELECT * FROM channel_table")
    fun getChannels() : LiveData<List<Channel>>

    @Query("SELECT name FROM channel_table")
    suspend fun waitChannels() : List<String>

    @Query("SELECT `key` FROM channel_table WHERE `name` = :name")
    suspend fun getKey(name : String) : String

    @Query("SELECT `contents` FROM channel_table WHERE `name` IN (:list)")
    fun getLiveContents(list : List<String>) : LiveData<List<String>>
}