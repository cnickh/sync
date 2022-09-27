package daemon.dev.field.cereal.objects

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "user_table")
data class User(

    /* Network Information */
    @PrimaryKey(autoGenerate = false) val key : String,
    var alias : String,
    val clout : Int,

    ){
    override fun equals(other: Any?): Boolean {
        val user = other as User
        return user.key == key
    }

    fun hash() : String{
        return key
    }
}

