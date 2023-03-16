package daemon.dev.field.nypt

import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.*
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.SecureRandom


class Signature {

    private lateinit var signer: Ed25519Signer
    private lateinit var privateKey : Ed25519PrivateKeyParameters
    lateinit var publicKey : Ed25519PublicKeyParameters

    private val RANDOM = SecureRandom()


    fun init(){

        val kpg = Ed25519KeyPairGenerator()
        kpg.init(Ed25519KeyGenerationParameters(RANDOM))

        val kp = kpg.generateKeyPair()
        privateKey = kp.private as Ed25519PrivateKeyParameters
        publicKey = kp.public as Ed25519PublicKeyParameters
        signer = Ed25519Signer()
        signer.init(true, privateKey)

    }

    fun init(publicKey : ByteArray, privateKey : ByteArray){

        this.privateKey = Ed25519PrivateKeyParameters(privateKey)
        this.publicKey = Ed25519PublicKeyParameters(publicKey)

        signer = Ed25519Signer()
        signer.init(true, this.privateKey)

    }

    fun getPublic() : ByteArray {
        return publicKey.encoded
    }

    fun getPrivate() : ByteArray {
        return privateKey.encoded
    }

    fun sign(message: ByteArray): ByteArray? {
        signer.update(message, 0, message.size)
        val ret = signer.generateSignature()
        signer.reset()
        return ret
    }

    fun sign(message: ByteArray, privateKey: Ed25519PrivateKeyParameters): ByteArray? {
        val signer = Ed25519Signer()
        signer.init(true, privateKey)
        signer.update(message,0,message.size)
        return signer.generateSignature()
    }

    fun verify(message: ByteArray, signature: ByteArray, publicKey: Ed25519PublicKeyParameters) : Boolean{
        val verifier = Ed25519Signer()
        verifier.init(false,publicKey)
        verifier.update(message,0,message.size)
        return verifier.verifySignature(signature)
    }


}