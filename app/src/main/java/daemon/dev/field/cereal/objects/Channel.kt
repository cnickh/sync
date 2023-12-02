package daemon.dev.field.cereal.objects

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "channel_table")
data class Channel(

    /* Network Information */
    @PrimaryKey(autoGenerate = false) val name : String,
    val key : String,

    )