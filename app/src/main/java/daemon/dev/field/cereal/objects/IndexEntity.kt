package daemon.dev.field.cereal.objects

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "index_table")
data class IndexEntity(
    val channel : String,
    val index : Int,
    @PrimaryKey(autoGenerate = false) val key : Int = 0
    )
