package daemon.dev.field.nypt

import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECParameterSpec
import java.security.*
import javax.crypto.Cipher

class Crypto {

    lateinit var pair : KeyPair
    private lateinit var dCipher : Cipher
    private lateinit var eCipher : Cipher

    fun init(){

        Security.setProperty("crypto.policy", "unlimited");


        val generator = KeyPairGenerator.getInstance("RSA")

        dCipher = Cipher.getInstance("RSA")
        eCipher = Cipher.getInstance("RSA")

        pair = generator.genKeyPair()

        dCipher.init(Cipher.DECRYPT_MODE,pair.private)
        eCipher.init(Cipher.ENCRYPT_MODE,pair.public)

        //Log.v("CRYPTO","Have: \n Public ${pair.public} \n Private ${pair.private}")

    }

    fun getPublic() : Key{
        return pair.public
    }

    fun encrypt(open: ByteArray, key: Key): ByteArray {

        val cipher = Cipher.getInstance("RSA")

        cipher.init(Cipher.ENCRYPT_MODE, key)

        return cipher.doFinal(open)
    }

    fun decrypt(closed : ByteArray): ByteArray {

        return dCipher.doFinal(closed)

    }


    fun main(args: Array<String>) {
        val curveParams = CustomNamedCurves.getByName("Curve25519")
        val ecSpec = ECParameterSpec(
            curveParams.curve,
            curveParams.g,
            curveParams.n,
            curveParams.h,
            curveParams.seed
        )
        val kpg = KeyPairGenerator.getInstance("EC", BouncyCastleProvider())
        kpg.initialize(ecSpec)
        val keyPair = kpg.generateKeyPair()
        val publicKey = keyPair.public
        val privateKey = keyPair.private
    }
}