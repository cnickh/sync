package daemon.dev.field.network

import android.util.Log
import daemon.dev.field.PACKER_TAG
import daemon.dev.field.data.objects.MeshRaw
import daemon.dev.field.data.objects.Wrapper
import daemon.dev.field.util.Serializer


/** Packer
 *  This class is made to shrink and wrap data packets so that they can be sent over
 *  BluetoothLE which as far as I can tell has a max packet size of 512 bytes.
 *
 */
class Packer(private val raw : MeshRaw) {

    private val PACKET_SIZE = 512

    private val serializer = Serializer()
    private var bytes : ByteArray = serializer.packetToByte(raw)
    private val size = bytes.size
    private val max = size/PACKET_SIZE
    private var packets = mutableListOf<ByteArray>()

    var sent : Int = -1

    init {

        val type = raw.type
        val mid = (1..9999).random()

        if(max == 0){
            Log.v(PACKER_TAG, "Using single wrapper")
            val wrap = Wrapper(type,mid,0,max,bytes)
            packets.add(serializer.wrapperToBytes(wrap))

        } else {
            Log.v(PACKER_TAG, "Using multiple wrappers")

            for (i in (0 until max)){
                val range = IntRange(PACKET_SIZE*i,PACKET_SIZE*(i+1))
                Log.v(PACKER_TAG, "have range: $range and size: $size")
                val wrap = Wrapper(type,mid,i,max,bytes.sliceArray(range))
                Log.v(PACKER_TAG, "bruh wtf is going on")
                packets.add(i,serializer.wrapperToBytes(wrap))
            }

            if(size%PACKET_SIZE != 0){
                val range = IntRange(max*PACKET_SIZE,size-1)
                Log.v(PACKER_TAG, "for remainder have range: $range and size: $size")
                val wrap = Wrapper(type,mid,max,max,bytes.sliceArray(range))
                packets.add(max,serializer.wrapperToBytes(wrap))
            }

        }

    }

    fun next() : ByteArray? {
        sent++
        return if(sent <= max){
            packets[sent]
        } else {
            null
        }
    }

}