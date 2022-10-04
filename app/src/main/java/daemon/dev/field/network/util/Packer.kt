package daemon.dev.field.network.util

import daemon.dev.field.CHARSET
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.Wrapper
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Packer
 *  This class is made to shrink and wrap data packets so that they can be sent over
 *  BluetoothLE which as far as I can tell has a max packet size of 512 bytes.
 *
 */
class Packer(private val raw : MeshRaw) {

    private val PACKET_SIZE = 365

    private var bytes : String = Json.encodeToString(raw)
    private val size = bytes.length
    private val max = size/PACKET_SIZE
    private var packets = mutableListOf<ByteArray>()

    var sent : Int = -1

    init {

        val type = raw.type
        val mid = (1..9999).random()

//        Log.v(PACKER_TAG, "packing with max:$max size:$size PACKET:$PACKET_SIZE")


        if(max == 0){
            val wrap = Wrapper(type,mid,0,max,bytes)
            packets.add(bytes(wrap))

        } else {

            for (i in (0 until max)){
                val range = IntRange(PACKET_SIZE*i,PACKET_SIZE*(i+1)-1)
                val wrap = Wrapper(type,mid,i,max,bytes.slice(range))
                packets.add(i,bytes(wrap))
            }

            if(size%PACKET_SIZE != 0){
                val range = IntRange(max*PACKET_SIZE,size-1)
                val wrap = Wrapper(type,mid,max,max,bytes.slice(range))
                packets.add(max,bytes(wrap))
            }

        }

    }

    private fun bytes(wrap : Wrapper) : ByteArray{
        val json  = Json.encodeToString(wrap)
//        Log.i("Packer.kt","Wrapping - $json")
        return json.toByteArray(CHARSET)
    }

    fun next() : ByteArray? {
        sent++
        return if(sent <= max){
//            Log.v(PACKER_TAG, "next(): size-${packets[sent].size}")
            packets[sent]
        } else {
            null
        }
    }

}