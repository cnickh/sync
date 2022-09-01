package daemon.dev.field.network

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
//        Log.i("Sorter.kt","Unwrapping - $json")
        val wrap = Json.decodeFromString<Wrapper>(json)

        return wrap.let{

            val mid = wrap.mid
            val cur = wrap.cur
            val data = wrap.bytes

//            Log.d("Sorter","Have $cur / ${wrap.max}")

            if(slots.keys.contains(mid)){
                slots[mid]!!.add(cur, data)
            } else {
                slots[mid] = mutableListOf()
                slots[mid]!!.add(cur,data)
            }

            if(slots[mid]!!.size == wrap.max+1){

//                Log.d("Sorter","cur == max")

                var raw = ""

                for (b in slots[mid]!!){
                    raw += b
                }

//                Log.w("Sorter","Decoding:\n $raw")
                Json.decodeFromString<MeshRaw>(raw)

            } else {
                null
            }

        }

    }

}