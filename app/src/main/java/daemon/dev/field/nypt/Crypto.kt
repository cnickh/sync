package daemon.dev.field.nypt

import android.util.Log
import java.security.*
import javax.crypto.*
import java.security.spec.EllipticCurve

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

class Crypto {

    lateinit var pair : KeyPair
    private lateinit var dCipher : Cipher
    private lateinit var eCipher : Cipher

    fun init(){

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

}