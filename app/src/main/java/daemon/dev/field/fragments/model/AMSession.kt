package daemon.dev.field.fragments.model

import android.util.Log
import daemon.dev.field.cereal.objects.Comment

/**
 * Used for holding a direct messaging session between two users.
 */

class AMSession(private val key : String) {

    private val inOrder = mutableListOf<Comment>()
    private var lastSeen = -1//Index of last comment seen

    fun sub() : List<Comment>{
        view()
        return inOrder
    }

    fun nonViewSub() : List<Comment> {
        return inOrder
    }

    fun print(){
        var s = ""
        for (i in inOrder.indices){
            s += inOrder[i].comment + inOrder[i].time.toString() + "\n"
        }
        Log.i("AMSession$key",s)
    }

    fun zeroSub(){
        for (c in inOrder){
            c.time = 0L
        }
    }

    fun view() {
        lastSeen = inOrder.lastIndex
    }

    fun unRead() : Int {
        return inOrder.lastIndex - lastSeen
    }

    fun addInOrder(msg : Comment){

        val time = msg.time

        for ((prev, m) in inOrder.withIndex()){
            if(time < m.time){
                inOrder.add(prev,msg)
                return
            }
        }

        inOrder.add(msg)
    }

//    private fun ByteArray.toBase64() : String {
//        return Base64.getEncoder().encodeToString(this)
//    }

}