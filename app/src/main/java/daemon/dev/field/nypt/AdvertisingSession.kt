package daemon.dev.field.nypt

import daemon.dev.field.PRIVATE_KEY
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.cereal.objects.KeyBundle
import daemon.dev.field.toBase64
import java.security.SecureRandom

class AdvertisingSession {

    private val RANDOM = SecureRandom()

    fun createSig() : KeyBundle {
        val signature = Signature()
        signature.init(PUBLIC_KEY, PRIVATE_KEY)

        val nonsense = ByteArray(2)
        RANDOM.nextBytes(nonsense)
        val sig = signature.sign(nonsense)!!

        return KeyBundle(nonsense.toBase64(),sig.toBase64())
    }


}