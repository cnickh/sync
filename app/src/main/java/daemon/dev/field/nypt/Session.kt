package daemon.dev.field.nypt

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters
import org.bouncycastle.crypto.util.PublicKeyFactory
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory
import java.security.SecureRandom


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

    private fun testAgreement() {
        val kpGen: AsymmetricCipherKeyPairGenerator = X25519KeyPairGenerator()
        kpGen.init(X25519KeyGenerationParameters(RANDOM))
        val kpA = kpGen.generateKeyPair()
        val kpB = kpGen.generateKeyPair()

        val agreeA = X25519Agreement()
        agreeA.init(kpA.private)
        val secretA = ByteArray(agreeA.agreementSize)
        agreeA.calculateAgreement(kpB.public, secretA, 0)

        val agreeB = X25519Agreement()
        agreeB.init(kpB.private)
        val secretB = ByteArray(agreeB.agreementSize)
        agreeB.calculateAgreement(kpA.public, secretB, 0)



//        if (!areEqual(secretA, secretB)) {
//            fail("X25519 agreement failed")
//        }
    }

}