package daemon.dev.field.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import daemon.dev.field.cereal.objects.Resource

@Dao
interface ResDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Resource)

    @Query("SELECT * from resource_table WHERE `key` = :key")
    suspend fun get(key: String): Resource?

    @Query("SELECT uri from resource_table WHERE `key` = :key")
    fun getResource(key: String): LiveData<String?>

    @Update
    suspend fun update(item: Resource)

    @Query("DELETE FROM resource_table WHERE `key` = :key")
    fun del(key : String)

    @Query("DELETE FROM resource_table")
    fun clear()

}