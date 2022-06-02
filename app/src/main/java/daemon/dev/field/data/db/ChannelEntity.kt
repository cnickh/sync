package daemon.dev.field.data.db

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import daemon.dev.field.nypt.AES_KEY_SIZE

class ChannelEntity(

    /* Network Information */
    @PrimaryKey(autoGenerate = false) var name : String,
    @ColumnInfo(name = "key") var key : ByteArray = ByteArray(AES_KEY_SIZE),

    )