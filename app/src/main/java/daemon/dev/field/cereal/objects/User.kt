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
    var channels : String,
    var Status : Int = 0

    ){

    override fun equals(other: Any?): Boolean {
        val user = other as User
        return user.key == key
    }

    fun hash() : String{
        return key
    }

    companion object{

        const val CONNECTED = 0
        const val BLOCKED = 1
        const val DISCONNECTED = 2

    }

}

