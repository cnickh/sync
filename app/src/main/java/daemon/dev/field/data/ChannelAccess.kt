package daemon.dev.field.data
import daemon.dev.field.cereal.objects.Channel
import daemon.dev.field.data.db.ChannelDao

class ChannelAccess(private val sync : ChannelDao) {

    val channels = sync.getChannels()


    fun clear(){
        sync.clear()
    }

    suspend fun delete(name : String){
        sync.delete(name)
    }

    suspend fun updateKey(name : String, key : String){
        sync.updateKey(name,key)
    }

//    suspend fun getContents(channels: List<String>) : List<String>{
//        val posts = mutableListOf<String>()
//
//        for(c in channels){
//
//            val content = sync.waitContents(c)
//
//            if(content!="null") {
//                for (p in content.split(",")) {
//                    if (!posts.contains(p)) posts.add(p)
//                }
//            }
//
//        }
//
//        return posts
//    }

    suspend fun createChannel(name : String, key : String){
        val ch = Channel(name, key)
        sync.insert(ch)
    }


    suspend fun key(name : String) : String {
        return sync.getKey(name)
    }

    suspend fun mapOpenChannels(channels: List<String>): HashMap<String,MutableList<String>>{

        val ch_map = hashMapOf<String,MutableList<String>>()

            for(c in channels){
                val content = listOf("null")//sync.waitContents(c).split(",")

                ch_map[c] = mutableListOf()

                if(content[0] != "null") {
                    for (p in content) {
                        ch_map[c]?.add(p)
                    }
                }

            }

        return ch_map
    }

//    suspend fun resolveChannels(channels : List<String>, open : List<String>) : HashMap<String,MutableList<String>>{
//
//        val ch_map = hashMapOf<String,MutableList<String>>()
//
//        for(c in channels){
//            if(open.contains(c)){
//                val content = sync.waitContents(c).split(",")
//
//                ch_map[c] = mutableListOf()
//
//                if(content[0] != "null") {
//                    ch_map[c] = mutableListOf()
//                    for (p in content) {
//                        ch_map[c]?.add(p)
//                    }
//                }
//
//            }
//        }
//
//        return ch_map
//    }


}