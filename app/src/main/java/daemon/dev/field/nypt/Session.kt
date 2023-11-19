package daemon.dev.field.nypt

import android.util.Log
import daemon.dev.field.GATT_RESOLVER_TAG
import daemon.dev.field.PRIVATE_KEY
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.cereal.objects.HandShake
import daemon.dev.field.cereal.objects.KeyBundle
import daemon.dev.field.toBase64
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters
import org.bouncycastle.crypto.util.PublicKeyFactory
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory
import java.security.SecureRandom
import java.util.Base64


class Session {

    private val RANDOM = SecureRandom()
    private lateinit var kp : AsymmetricCipherKeyPair

    fun generateSecret(): ByteArray {
        val kpGen = X25519KeyPairGenerator()
        kpGen.init(X25519KeyGenerationParameters(RANDOM))
        kp = kpGen.generateKeyPair()
        val publicKeyInfo: SubjectPublicKeyInfo =
            SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(kp.public)

        return publicKeyInfo.encoded
    }

    fun computeAgreement(secret : ByteArray) : ByteArray{
        val agree = X25519Agreement()
        agree.init(kp.private)
        val sessionSecret = ByteArray(agree.agreementSize)
        val sharedKey = PublicKeyFactory.createKey(secret)
        agree.calculateAgreement(sharedKey, sessionSecret, 0)

        return sessionSecret
    }

    fun createSecret() : KeyBundle {
        val signature = Signature()
        signature.init(PUBLIC_KEY, PRIVATE_KEY)

        val secret = generateSecret()
        val sig = signature.sign(secret)!!

        return KeyBundle(secret.toBase64(),sig.toBase64())
    }

    fun computeSharedKey(keyBundle : KeyBundle, key : String) : ByteArray?{
        val publicKey = Ed25519PublicKeyParameters(key.toByteArray())
        val secret = keyBundle.secret
        val sig = keyBundle.sig

        val verified = Signature().verify(secret.toByteArray(),sig.toByteArray(),publicKey)

        return if(verified){
            computeAgreement(secret.toByteArray())
        }else{
            Log.e(GATT_RESOLVER_TAG,"handleResolverEvent: verification Failed")
            null
        }
    }

//    private fun testAgreement() {
//        val kpGen: AsymmetricCipherKeyPairGenerator = X25519KeyPairGenerator()
//        kpGen.init(X25519KeyGenerationParameters(RANDOM))
//        val kpA = kpGen.generateKeyPair()
//        val kpB = kpGen.generateKeyPair()
//
//        val agreeA = X25519Agreement()
//        agreeA.init(kpA.private)
//        val secretA = ByteArray(agreeA.agreementSize)
//        agreeA.calculateAgreement(kpB.public, secretA, 0)
//
//        val agreeB = X25519Agreement()
//        agreeB.init(kpB.private)
//        val secretB = ByteArray(agreeB.agreementSize)
//        agreeB.calculateAgreement(kpA.public, secretB, 0)
//
//    }

}