package daemon.dev.field

import daemon.dev.field.nypt.Crypto
import junit.framework.Assert.assertNotNull
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CryptoTestSuite {

    lateinit var crypt : Crypto

    @Before
    fun generateKeys(){
        crypt = Crypto()
        crypt.init()
        assertNotNull(crypt)
    }

    @Test
    fun symmetricEncrypt(){

    }

    @Test
    fun publicEncrypt(){

        val test = "This is a test string for testing"
        println(test)

        val bytes = test.toByteArray(CHARSET)

        val cipher = crypt.encrypt(bytes,crypt.getPublic())

        println("cipher: ${cipher.toHexString()}")

        val recovered = crypt.decrypt(cipher)

        println("recovered: ${recovered.toHexString()}")

        Assert.assertEquals(test,recovered.toString(CHARSET))
    }


    private fun ByteArray.toHexString() : String {
        return this.joinToString("") { it.toString(16) }
    }
}