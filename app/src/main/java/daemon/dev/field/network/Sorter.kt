package daemon.dev.field.network

import android.util.Log
import daemon.dev.field.data.objects.MeshRaw
import daemon.dev.field.util.Serializer

class Sorter {

    private val slots = mutableMapOf<Int, MutableList<ByteArray>>()
    private val serializer = Serializer()

    fun resolve(bytes : ByteArray) : MeshRaw? {

        val wrap = serializer.bytesToWrapper(bytes)

        return wrap?.let{

            val mid = wrap.mid
            val cur = wrap.cur
            val data = wrap.bytes

            Log.d("Sorter","Have $cur / ${wrap.max}")

            if(slots.keys.contains(mid)){
                slots[mid]!!.add(cur, data)
            } else {
                slots[mid] = mutableListOf()
                slots[mid]!!.add(cur,data)
            }

            if(cur == wrap.max){

                Log.d("Sorter","cur == max")

                var raw : ByteArray = byteArrayOf()

                for (b in slots[mid]!!){
                    raw += b
                }

                serializer.getPacket(raw)
            } else {
                null
            }

        }

    }

}