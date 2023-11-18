package daemon.dev.field.cereal.objects

import kotlinx.serialization.Serializable

@Serializable
class MeshRaw(val type : Int,
              val nodeInfo : User? = null,
              val requests : List<String>? = null, //List<Address>
              val newData : HashMap<String,HashMap<String,String>>? = null, //HashMap<Address,HashCode>
              val posts : List<Post>? = null,
              val misc : String? = null
                    ){

    var mid : Int = 0

    companion object{

        const val INFO = 0
        const val POST_LIST = 1
        const val POST_W_ATTACH = 2
        const val REQUEST = 3
        const val NEW_DATA = 4
        const val PING = 5
        const val DISCONNECT = 6
        const val CONFIRM = 7

        const val CHANNEL = 8
        const val DIRECT = 9

        const val DECODE_ERROR = 10
        const val DECODE_ERROR_ = 11
    }

    fun hash(): String {
        return type.toString() + nodeInfo?.hash() + requests?.toString() + newData?.toString() + posts?.toString() + misc
    }

}