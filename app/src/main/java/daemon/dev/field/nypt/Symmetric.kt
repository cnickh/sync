package daemon.dev.field.nypt

import org.bouncycastle.crypto.CipherParameters
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.modes.SICBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.bouncycastle.util.encoders.Hex
import java.security.SecureRandom


class Symmetric {

    lateinit var engine : SICBlockCipher
    lateinit var decrypt : SICBlockCipher

    fun init(key : ByteArray){
        val params: CipherParameters = ParametersWithIV(
            KeyParameter(key),
            Hex.decode("000000000000000000000000000000")
        )

        engine = SICBlockCipher(AESEngine())
        decrypt = SICBlockCipher(AESEngine())
        engine.init(true,params)
        decrypt.init(false,params)
    }

    fun encrypt(message : ByteArray) : ByteArray{
        val cipher = ByteArray(message.size)
        engine.processBytes(message, 0, message.size, cipher, 0)
        engine.reset()
        return cipher
    }

    fun decrypt(message: ByteArray) : ByteArray{
        val plain = ByteArray(message.size)
        decrypt.processBytes(message, 0, message.size, plain, 0)
        decrypt.reset()
        return plain
    }

    private fun ctrCounterTest() {
        val params: CipherParameters = ParametersWithIV(
            KeyParameter(Hex.decode("5F060D3716B345C253F6749ABAC10917")),
            Hex.decode("000000000000000000000000000000")
        )
        val engine = SICBlockCipher(AESEngine())
        engine.init(true, params)
        val rand = SecureRandom()
        val cipher = ByteArray(256 * 16)
        var plain = ByteArray(255 * 16)
        rand.nextBytes(plain)
        engine.processBytes(plain, 0, plain.size, cipher, 0)
        engine.init(true, params)
        plain = ByteArray(256 * 16)
        engine.init(true, params)
        try {
            engine.processBytes(plain, 0, plain.size, cipher, 0)
            //fail("out of range data not caught")
        } catch (e: IllegalStateException) {
            if ("Counter in CTR/SIC mode out of range." != e.message) {
               // fail("wrong exception")
            }
        }
    }
}