package daemon.dev.field.nypt

import daemon.dev.field.CHARSET
import java.security.MessageDigest

class ChannelBuilder(val name : String) {

    fun key() : ByteArray{

        val msgDgst: MessageDigest = MessageDigest.getInstance("SHA-256")

        return msgDgst.digest(name.toByteArray(CHARSET))
    }

}