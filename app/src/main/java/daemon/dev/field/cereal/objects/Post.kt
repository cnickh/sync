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

    override fun equals(other: Any?): Boolean {
        return (other as Post).address() == address()
    }

    fun contentString() : String{
        return title + body + comment
    }

    fun address() : Address{
        return Address("$key:$time")
    }

}
