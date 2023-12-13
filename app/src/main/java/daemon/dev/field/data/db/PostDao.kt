package daemon.dev.field.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import daemon.dev.field.cereal.objects.IndexEntity
import daemon.dev.field.cereal.objects.Post


@Dao
interface PostDao {

    @Insert
    suspend fun insert(item: Post)

    @Query("SELECT * from post_table WHERE `index` = :index")
    suspend fun get(index: Int): Post

    @Query("SELECT * from post_table WHERE `index` = :index")
    fun getLive(index: Int): LiveData<Post>

    @Query("SELECT * FROM post_table WHERE `key` = :key AND `time` = :time")
    suspend fun getAt(key : String, time : Long) : Post?

    @Query("DELETE FROM post_table WHERE `index` = :index")
    fun del(index: Int)

    @Query("DELETE FROM post_table")
    fun clear()
    @Query("DELETE FROM index_table")
    fun clearIndexes()
    @Query("SELECT * FROM post_table ORDER BY time DESC LIMIT 1")
    suspend fun getMostRecentItem(): Post?

    @Query("SELECT * FROM post_table ORDER BY time DESC")
    fun getPosts(): LiveData<List<Post>>

    @Query("SELECT * FROM post_table WHERE `key` IN (:list)")
    fun getLivePosts(list : List<String>) : LiveData<List<Post>>

    @Query("SELECT * FROM post_table WHERE `index` IN ( SELECT `index` FROM index_table WHERE `channel` IN (:list))")
    fun getListPostFromChannelQuery(list : List<String>) : LiveData<List<Post>>

    @Query("SELECT `index` FROM index_table WHERE `channel` IN (:list)")
    fun testListFromChannel(list : List<String>) : LiveData<List<Int>>

    @Query("SELECT `index` FROM index_table WHERE `channel` = :channel")
    suspend fun postsInChannel(channel : String) : List<Int>

    @Query("SELECT `index` FROM index_table WHERE `channel` = :channel")
    suspend fun getPostsInChannels(channel : String) : List<Int>

    @Insert
    fun addIndex(index : IndexEntity)

    @Query("SELECT * from post_table WHERE `key` = :key")
    fun getByKey(key: String): LiveData<List<Post>>

    @Query("SELECT COUNT(*) FROM post_table WHERE `key` = :key")
    suspend fun getKey(key: String) : Int

    @Query("UPDATE post_table SET comment=:nwComment WHERE `index` = :index")
    suspend fun updateComment(nwComment: String?, index: Int)

    @Update
    suspend fun update(post : Post)

}
