package daemon.dev.field.util

import java.util.*

class BaseUtils {
    fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
    }

    fun String.toByteArray() : ByteArray {
        return Base64.getDecoder().decode(this)
    }
}