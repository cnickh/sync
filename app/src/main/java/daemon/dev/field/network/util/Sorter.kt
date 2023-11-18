package daemon.dev.field.network.util

import android.util.Log
import daemon.dev.field.CHARSET
import daemon.dev.field.cereal.objects.HandShake
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.Wrapper
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.Base64

class Sorter {

    private val slots = mutableMapOf<Int, ArrayList<String>>()
    private fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
    }
    fun resolve(bytes : ByteArray) : MeshRaw? {
        val json = bytes.toString(CHARSET)

        val wrap = try {
            Json.decodeFromString<Wrapper>(json)
        } catch (e : Exception) {
            try {
                val shake = Json.decodeFromString<HandShake>(json)
                Log.e("Sorter.kt","Could not decode wrapper, is handshake \n have $shake")
            } catch (e : Exception) {
                Log.e("Sorter.kt","Could not decode wrapper, Not HandShake! Bad Key? \n have ${bytes.toBase64()}")
                null
            }
            null
        }

        return wrap?.let{

            val mid = wrap.mid
            val cur = wrap.cur
            val data = wrap.bytes
            val max = wrap.max

//            Log.v("Sorter.kt","have mid: $mid cur: $cur max: $max")

            if(slots.keys.contains(mid)){
                try {
                    slots[mid]!!.add(cur,data)
                } catch (e : Exception) {
                Log.e("Sorter.kt","ERROR PACKETS OUT OF ORDER" +
                        "\n mid: $mid cur: $cur max: $max")
                }
            } else {
                if(cur == 0){
                    slots[mid] = ArrayList(max)
                    slots[mid]!!.add(cur, data)
                }
            }

            if(slots[mid]?.size == max){

                var raw = ""

                for (b in slots[mid]!!){
                    raw += b
                }

                slots.remove(mid)
                try {
                    Json.decodeFromString<MeshRaw>(raw)
                } catch (e : Exception){
                    MeshRaw(MeshRaw.DECODE_ERROR_)
                }

            } else {
                null
            }

        }

    }

}