package daemon.dev.field.cereal.objects

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = "post_table")
data class Post(

    val key : String,
    val time : Long,
    val title : String,
    val body : String,
    var comment : String,
    var hops : Int,
    @PrimaryKey(autoGenerate = true) var index : Int = 0,

    ){
    fun hash() : String{
        val hash = title + body + comment
        return hash.hashCode().toString()
    }
    fun address() : Address{
        return Address("$key:$time")
    }
}
