package daemon.dev.field.util

import daemon.dev.field.CHARSET
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.cereal.objects.Post
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*



class Gen {

    fun commentPost(post : Post, len : Int){

    }

    fun genPost(len : Int) : Post{

        val title = RandomString(10).nextString()
        val body = RandomString(len).nextString()

        return Post(
            PUBLIC_KEY.toString(CHARSET), System.currentTimeMillis(),
            title,
            body,
            "null",
            0, 0,
        )
    }

    fun getDateTime(s: Long): String? {
        return try {
            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
            val netDate = Date(s)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

    class RandomString(length: Int, random: Random?, symbols: String) {
        /**
         * Generate a random string.
         */
        fun nextString(): String {
            for (idx in buf.indices) buf[idx] = symbols[random.nextInt(symbols.size)]
            return String(buf)
        }

        private val random: Random
        private val symbols: CharArray
        private val buf: CharArray

        init {
            require(length >= 1)
            require(symbols.length >= 2)
            this.random = Objects.requireNonNull(random)!!
            this.symbols = symbols.toCharArray()
            buf = CharArray(length)
        }
        /**
         * Create an alphanumeric string generator.
         */
        /**
         * Create an alphanumeric strings from a secure generator.
         */
        /**
         * Create session identifiers.
         */
        @JvmOverloads
        constructor(length: Int = 21, random: Random? = SecureRandom()) : this(
            length,
            random,
            alphanum
        )

        companion object {
            const val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            val lower = upper.lowercase(Locale.ROOT)
            const val digits = "0123456789"
            val alphanum = upper + lower + digits
        }
    }

}
