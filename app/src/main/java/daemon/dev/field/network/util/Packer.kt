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
class Packer(raw : MeshRaw) {

    private val PACKET_SIZE = 365

    private var bytes : String = Json.encodeToString(raw)
    val size = bytes.length
    private var max = size/PACKET_SIZE
    private var packets = mutableListOf<ByteArray>()
    private var iterations = 0

    private var sent : Int = -1

    init {

        val type = raw.type
        val mid = (1..9999).random()

        if(max == 0){
            max++
            val wrap = Wrapper(type,mid,0,max,bytes)
            packets.add(bytes(wrap))

        } else {

            if(size%PACKET_SIZE!=0) max++

            for (i in (0 until max)){
                val range = if(i == max-1){
                    IntRange(PACKET_SIZE*i,size-1)
                } else {
                    IntRange(PACKET_SIZE*i,PACKET_SIZE*(i+1)-1)
                }

                val wrap = Wrapper(type,mid,i,max,bytes.slice(range))
                packets.add(bytes(wrap))
                iterations++
            }

        }

    }

    private fun bytes(wrap : Wrapper) : ByteArray{
        val json  = Json.encodeToString(wrap)
        return json.toByteArray(CHARSET)
    }

    fun next() : ByteArray? {
        sent++
//        Log.i("Packer.kt", "vars :: sent: $sent max: $max packets.size: ${packets.size}" +
//                "\n iterations: $iterations size: $size PACKET_SIZE: $PACKET_SIZE")
        return if(sent < max){
            packets[sent]
        } else {
            null
        }
    }

    fun count() : Int {
        return max
    }

}