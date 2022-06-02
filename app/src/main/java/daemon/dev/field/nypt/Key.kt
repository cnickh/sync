package daemon.dev.field.nypt

import android.util.Log
import java.security.SecureRandom

class Key(private var size : Int, private val value : ByteArray? = null) {

    private var key : ByteArray

    init {
        if(value == null) {
            key = ByteArray(size)
            val random = SecureRandom()
            random.nextBytes(key)
        } else {
            key = value
        }
        Log.v(KEY_TAG,"create new key size: $size")
    }

    fun decodeHex(hex : String){
        hex.let {

            key = it.chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()
        }
        size = key.size
    }

    fun get() : ByteArray {
        return key
    }

    fun size() : Int {
        return size
    }

    fun toHex(): String =
        key.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

    fun cmp(key : Key) : Boolean{
        val array = key.get()
        for(b in array.indices){
            if(this.key[b] != array[b]){return false}
        }

        return true
    }

}