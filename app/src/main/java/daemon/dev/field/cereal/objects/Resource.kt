package daemon.dev.field.cereal.objects

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "resource_table")
data class Resource(

    @PrimaryKey(autoGenerate = false)var key : String,

    var uri : String

    )
