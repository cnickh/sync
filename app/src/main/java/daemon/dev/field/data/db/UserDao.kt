package daemon.dev.field.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.cereal.objects.User
import java.util.*

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: User)

    @Update
    suspend fun update(item: User)

    @Query("UPDATE user_table SET status=:status WHERE `key` = :key")
    suspend fun updateStatus(key: String, status: Int)

    @Query("UPDATE user_table SET alias=:nwAlias WHERE `key` = :key")
    suspend fun setAlias( nwAlias : String, key : String = PUBLIC_KEY.toBase64())

    @Query("SELECT * FROM user_table WHERE `key` IN (:list)")
    fun getUsers(list : List<String>) : LiveData<List<User>>

    @Query("SELECT * FROM user_table ORDER BY clout DESC")
    fun getAll(): List<User>

    @Query("SELECT * from user_table WHERE `key` = :key")
    fun get(key: String) : LiveData<User>

    @Query("SELECT `key` from user_table WHERE `status` = :status")
    suspend fun getUserWithStatus(status : Int) : List<String>

    @Query("SELECT * from user_table WHERE `key` = :key")
    suspend fun wait(key: String) : User?

    @Query("DELETE FROM user_table")
    fun clear()


    fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
    }

}