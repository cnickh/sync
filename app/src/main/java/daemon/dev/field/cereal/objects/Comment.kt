package daemon.dev.field.cereal.objects

import kotlinx.serialization.Serializable

@Serializable
class Comment(val key : String, val comment:String, var time:Long){

    val sub : MutableList<Comment> = mutableListOf()

    fun add(cmnt : Comment){
        sub.add(cmnt)
    }

    override fun equals(other: Any?): Boolean {
        val cmnt = other as Comment

        val _this = (key + comment + time.toString()).hashCode()
        val _that = (cmnt.key + cmnt.comment + cmnt.time.toString()).hashCode()

        return _this == _that
    }

}