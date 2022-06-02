package daemon.dev.field.data.objects

class MeshRaw(val type : Int,
              val nodeInfo : NodeInfo?,
              val requests : List<ULong>?,
              val newData : HashMap<ULong,String>?,
              val posts : List<Post>?,
              val extraData : ByteArray?
                    ){

    companion object{

        const val INFO = 0
        const val POST_LIST = 1
        const val POST_W_ATTACH = 2
        const val REQUEST = 3
        const val NEW_DATA = 4
        const val PING = 5
        const val DISCONNECT = 6

    }

}