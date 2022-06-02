package daemon.dev.field.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "post_table")
class PostEntity(

    /*Display Information*/
    @ColumnInfo(name = "hops") var hops : Int = 0,
    @ColumnInfo(name = "title") var title : String = "null",
    @ColumnInfo(name = "body") var body : String = "null",
    @ColumnInfo(name = "cString") var cString : String = "null",
    @ColumnInfo(name = "uri") var uri : String = "null",


    /* Network Information */
    @PrimaryKey(autoGenerate = true) var mid : Long = 0L,
    @ColumnInfo(name = "user") var user : String = "anon",
    @ColumnInfo(name = "time_created") var time_created : Long = 0,
    @ColumnInfo(name = "last_touched") var last_touched : Long = 0,


    )
