package daemon.dev.field.network.util

import android.util.Log
import daemon.dev.field.CHARSET
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.Wrapper
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class Sorter {

    private val slots = mutableMapOf<Int, MutableList<String>>()

    fun resolve(bytes : ByteArray) : MeshRaw? {
        val json = bytes.toString(CHARSET)

        val wrap = try {
            Json.decodeFromString<Wrapper>(json)
        } catch (e : Exception) {
            Log.e("Sorter.kt","Could not decode wrapper, possibly handshake \n have $json")
            null
        }

        return wrap?.let{

            val mid = wrap.mid
            val cur = wrap.cur
            val data = wrap.bytes
            val max = wrap.max

            Log.v("Sorter.kt","have mid: $mid cur: $cur max: $max")

            if(slots.keys.contains(mid)){
                slots[mid]!!.add(cur,data)
            } else {
                if(cur == 0){
                    slots[mid] = mutableListOf()
                    slots[mid]!!.add(cur,data)
                }
            }

            if(slots[mid]?.size == max+1){

                var raw = ""

                for (b in slots[mid]!!){
                    raw += b
                }

                slots.remove(mid)
                Json.decodeFromString<MeshRaw>(raw)

            } else {
                null
            }

        }

    }

}