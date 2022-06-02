package daemon.dev.field.util

import daemon.dev.field.data.objects.Comment

class CommentMerge(a : String, b : String) {

    private var ret : String
    private val cereal = Serializer()
    private val composed : MutableList<Comment> = mutableListOf()


    init {
        val list0 = cereal.commentListFromString(a)
        val list1 = cereal.commentListFromString(b)

        for (c in list0){
            composed.add(c)
        }

        for (c in list1){
            var here = false
            for(comp in composed){
                if(hash(c) == hash(comp)){
                    here = true
                    compose(c.commentList,comp.commentList)
                }

            }
            if(!here){
                composed.add(c)
            }
        }


        ret = cereal.commentListToString(composed)
    }

    private fun compose(from : List<Comment>, to : MutableList<Comment>){

        for (c in from){
            var here = false
            for(comp in to){

                if(hash(c) == hash(comp)){
                    here = true
                    compose(c.commentList,comp.commentList)
                }

            }
            if(!here){
                to.add(c)
            }
        }

    }

    private fun hash(cmnt : Comment) : Int{

        val i = cmnt.user.uid.get().toString()
        val c = cmnt.comment
        val t = cmnt.time_created.toString()

        return (i+c+t).hashCode()
    }

    fun getResult() : String{
        return ret
    }

}