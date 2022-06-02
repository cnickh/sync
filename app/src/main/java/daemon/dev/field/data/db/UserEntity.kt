package daemon.dev.field.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
class UserEntity(

    /* Network Information */
    @PrimaryKey(autoGenerate = false) var uid : String,
    @ColumnInfo(name = "alias") var alias : String = "anon",
    )

