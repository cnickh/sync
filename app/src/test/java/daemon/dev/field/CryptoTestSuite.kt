package daemon.dev.field

import daemon.dev.field.cereal.objects.KeyBundle
import daemon.dev.field.nypt.Session
import daemon.dev.field.nypt.Signature
import daemon.dev.field.nypt.Symmetric
import junit.framework.Assert.assertNotNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.security.SecureRandom
import java.util.*

class CryptoTestSuite {


    @Test
    fun signature(){
        val RANDOM = SecureRandom()

        val signature = Signature()

        signature.init()

        println("Size ${signature.getPublic().size} : "+signature.getPublic().toHexString())

        val data = ByteArray(20)
        RANDOM.nextBytes(data)

        val sig = signature.sign(data)!!

        Assert.assertEquals(true,signature.verify(data,sig,signature.publicKey))
    }

    @Test
    fun keyExchange(){
        val sessionA = Session()
        val sessionB = Session()

        val secretA = sessionA.generateSecret()
        val secretB = sessionB.generateSecret()

        val sharedA = sessionA.computeAgreement(secretB)
        val sharedB = sessionB.computeAgreement(secretA)

        val stringA = sharedA.toHexString()
        val stringB = sharedB.toHexString()

        println("Size: ${sharedA.size} A: $stringA")
        println("Size: ${sharedB.size} B: $stringB")

        Assert.assertEquals(stringA,stringB)
    }

    @Test
    fun symmetric(){
        val key = ByteArray(32)

        val symmetric = Symmetric()
        symmetric.init(key)

        val plain = ByteArray(128)

        val RANDOM = SecureRandom()
        RANDOM.nextBytes(plain)

        println("plain: " + plain.toHexString())

        val cipher = symmetric.encrypt(plain)

        println("cipher: " + cipher.toHexString())

        val recovered = symmetric.decrypt(cipher)

        println("recovered: " + recovered.toHexString())

        Assert.assertEquals(plain.toHexString(),recovered.toHexString())

    }

    @Test
    fun serialTestSuite(){

        //generate params

        val signerA = Signature()
        val signerB = Signature()

        val sessionA = Session()
        val sessionB = Session()

        signerA.init()
        signerB.init()

        val secretA = sessionA.generateSecret()
        val secretB = sessionB.generateSecret()

        val sigA = signerA.sign(secretA)!!
        val sigB = signerB.sign(secretB)!!
        Base64.getEncoder().encodeToString(sigB)

        //encode

        val keyA = KeyBundle(secretA.toBase64(),sigA.toBase64())
        val keyB = KeyBundle(secretB.toBase64(),sigB.toBase64())

        println("secret string length: ${keyA.secret.length}")
        println("sig string length: ${keyA.sig.length}")

        val jsonA = Json.encodeToString(keyA)
        val jsonB = Json.encodeToString(keyB)

        //decode

        val resA = Json.decodeFromString<KeyBundle>(jsonA)
        val resB = Json.decodeFromString<KeyBundle>(jsonB)

        val rSecretA = resA.secret.toByteArray()
        val rSecretB = resB.secret.toByteArray()

        println("secret byte length: ${rSecretA.size}")

        val rSigA = resA.sig.toByteArray()
        val rSigB = resB.sig.toByteArray()

        println("sig byte length: ${rSigA.size}")

        Assert.assertEquals(rSecretA.toHexString(),secretA.toHexString())
        Assert.assertEquals(rSecretB.toHexString(),secretB.toHexString())
        Assert.assertEquals(rSigA.toHexString(),sigA.toHexString())
        Assert.assertEquals(rSigB.toHexString(),sigB.toHexString())

        val verifiedA = Signature().verify(rSecretA,rSigA,signerA.publicKey)
        val verifiedB = Signature().verify(rSecretB,rSigB,signerB.publicKey)

        Assert.assertEquals(true,verifiedA)
        Assert.assertEquals(true,verifiedB)

    }

    @Test
    fun realShit(){

        val key = "PMECNdnc2zhhb0vj4a4zZBtr1IEZzM+tMGcPWGfJs44=".toByteArray()
        val secret = "MCowBQYDK2VuAyEACMmn3YNRHWJl1QIlme7nUSbgXoZGB+ZUn2a96OBqnUU=".toByteArray()
        val sig = "BBNYxI7tP0cNileeWVbaRuy0GJ4OKgjjxZ+a6SqbHL3lu6sElA73idx0p9jwG1vo7/gKadgjYdjpWdKXdfDSBQ==".toByteArray()

        val pub = Ed25519PublicKeyParameters(key)

        val status = Signature().verify(secret,sig,pub)

        Assert.assertEquals(true,status)

    }


    private fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
    }

    private fun String.toByteArray() : ByteArray {
        return Base64.getDecoder().decode(this)
    }

    private fun ByteArray.toHexString() : String {
        return this.joinToString("") { it.toString(16) }
    }
}