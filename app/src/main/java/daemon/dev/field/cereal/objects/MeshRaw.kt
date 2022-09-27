package daemon.dev.field.cereal.objects

import daemon.dev.field.CHARSET
import kotlinx.serialization.Serializable

@Serializable
class MeshRaw(val type : Int,
              val nodeInfo : User?,
              val requests : List<String>?, //List<Address>
              val newData : HashMap<String,String>?, //HashMap<Address,HashCode>
              val posts : List<Post>?,
              val misc : ByteArray?
                    ){

    companion object{

        const val INFO = 0
        const val POST_LIST = 1
        const val POST_W_ATTACH = 2
        const val REQUEST = 3
        const val NEW_DATA = 4
        const val PING = 5
        const val DISCONNECT = 6
        const val CONFIRM = 7

    }

    fun hash() : String {
        val sting = type.toString() + nodeInfo?.hash() + requests?.toString() + newData?.toString() + posts?.toString() + misc?.toString(CHARSET)
        return sting
    }

}