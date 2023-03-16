package daemon.dev.field.util


import android.util.Log
import daemon.dev.field.cereal.objects.Comment
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class CommentMerge(a : String, b : String) {

    var ret : String
    private val composed : MutableList<Comment> = mutableListOf()

    init {

        Log.i("merge.kt","Comments: \n a: $a \n b: $b")

        val list0 = if(a == "null"){
            mutableListOf()
        }else{
            Json.decodeFromString<List<Comment>>(a)
        }

        val list1 = if(b == "null"){
            mutableListOf()
        }else{
            Json.decodeFromString<List<Comment>>(b)
        }


        for (c in list0){
            composed.add(c)
        }

        for (c in list1){
            var here = false
            for(comp in composed){
                if(hash(c) == hash(comp)){
                    here = true
                    compose(c.sub,comp.sub)
                }
            }
            if(!here){
                place(composed,c)
            }
        }

        ret = Json.encodeToString(composed)

        Log.i("merge.kt","\n ret: $ret")
    }

    private fun compose(from : List<Comment>, to : MutableList<Comment>){

        for (c in from){
            var here = false
            for(comp in to){

                if(hash(c) == hash(comp)){
                    here = true
                    compose(c.sub,comp.sub)
                }

            }
            if(!here){
                place(to,c)
            }
        }

    }

    private fun place(list : MutableList<Comment>, item : Comment){
        if(list.size==0){list.add(item);return}

        val time = item.time

        for(i in list.indices){
            val t = list[i].time

            if(time < t){
                list.add(i,item)
                break
            }else if(i==list.size-1){
                list.add(item)
            }
        }

    }

    private fun hash(cmnt : Comment) : Int{
        val k = cmnt.key
        val c = cmnt.comment
        val t = cmnt.time.toString()

        return (k+c+t).hashCode()
    }

    fun getResult() : String{
        return if(ret == "[]"){
            "null"
        }else{
            ret
        }
    }

}