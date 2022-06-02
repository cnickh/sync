package daemon.dev.field.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface ItemDatabaseDao {

    @Insert
    fun insert(item: PostEntity)

    @Query("SELECT * from post_table WHERE mid = :key")
    fun get(key: Long): PostEntity?

    @Query("DELETE FROM post_table WHERE mid = :key")
    fun del(key: Long)

    @Query("DELETE FROM post_table")
    fun clear()

    @Query("SELECT * FROM post_table ORDER BY time_created DESC LIMIT 1")
    fun getMostRecentItem(): PostEntity?

    @Query("SELECT * FROM post_table ORDER BY mid DESC")
    fun getAllItems(): LiveData<List<PostEntity>>

    @Query("UPDATE post_table SET cString=:nwComment WHERE mid = :id")
    fun updateComment(nwComment: String?, id: Long)

}
