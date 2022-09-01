package daemon.dev.field.nypt

import kotlinx.serialization.Serializable
import java.security.SecureRandom

@Serializable
class Key(val key : String) {

    var bytes : ByteArray

    init {
        key.let {
            bytes = it.chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()
        }
    }

    fun get() : String{
        return key
    }

//    fun toHex(): String =
//        key.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }


}