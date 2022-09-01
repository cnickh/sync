package daemon.dev.field

import daemon.dev.field.cereal.objects.*
import daemon.dev.field.nypt.Key
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test

class JsonTestSuite {

    @Test
    fun comment() {
        val comment:String = "comment"
        val time:Long = 0
        val num:Int = 0
        val key = "a8cd12"
        val sub = Comment("a8kjl",comment,time)

        var obj = Comment("a8cd12",comment,time)
        obj.add(sub)

        val string = Json.encodeToString(obj)
        println(string)
        obj = Json.decodeFromString(string)

        Assert.assertEquals(key, obj.key)
        Assert.assertEquals(comment, obj.comment)
        Assert.assertEquals(time, obj.time)
        Assert.assertEquals(listOf(sub), obj.sub)

    }

    @Test
    fun meshRaw() {
        val type : Int = 0
        val nodeInfo = User("userKey","true",0)
        val requests : List<String> = listOf()
        val newData : HashMap<String,String> = hashMapOf()
        val posts : List<Post> = listOf()
        val misc : ByteArray = byteArrayOf()

        var obj = MeshRaw(type,nodeInfo,requests,newData,posts,misc)
//        class MeshRaw(val type : Int,
//                      val nodeInfo : User?,
//                      val requests : List<Address>?,
//                      val newData : HashMap<Address,String>?,
//                      val posts : List<Post>?,
//                      val misc : ByteArray?
//        ){
        val string = Json.encodeToString(obj)
        println(string)
        obj = Json.decodeFromString(string)

        Assert.assertEquals(type, obj.type)
        Assert.assertEquals(nodeInfo, obj.nodeInfo)
        Assert.assertEquals(requests, obj.requests)
        Assert.assertEquals(newData, obj.newData)
        Assert.assertEquals(posts, obj.posts)
        Assert.assertArrayEquals(misc, obj.misc)

    }

    @Test
    fun message() {
        val title = "title"
        val body = "body"
        val time: Long = 0

        var obj = Post(PUBLIC_KEY,time,title,body,"null",0)

        val string = Json.encodeToString(obj)
        println(string)
        obj = Json.decodeFromString(string)

        Assert.assertEquals(title, obj.title)
        Assert.assertEquals(body, obj.body)
        Assert.assertEquals(time, obj.time)
        Assert.assertEquals("user", obj.key)

    }

//    @Test
//    fun nodeInfo() {
//        val forwarding = true
//        val accepting = true
//        val connections: List<ULong> = listOf()
//        val channels : List<String> = listOf()
//
//        var obj = NodeInfo("user",forwarding,accepting,connections,channels)
//
//        val string = Json.encodeToString(obj)
//        println(string)
//        obj = Json.decodeFromString(string)
//
//        Assert.assertEquals("user", obj.key)
//        Assert.assertEquals(forwarding, obj.fwd)
//        Assert.assertEquals(accepting, obj.accept)
//        Assert.assertEquals(connections, obj.con)
//        Assert.assertEquals(channels, obj.channels)
//
//    }

    @Test
    fun wrapper() {
        val type : Int = 0
        val mid : Int = 0
        val cur : Int = 0
        val max : Int = 0
        val bytes = "byteArrayOf()"

        var obj = Wrapper(type,mid,cur,max,bytes)

        val string = Json.encodeToString(obj)
        println(string)
        obj = Json.decodeFromString(string)

        Assert.assertEquals(type, obj.type)
        Assert.assertEquals(mid, obj.mid)
        Assert.assertEquals(cur, obj.cur)
        Assert.assertEquals(max, obj.max)
        Assert.assertEquals(bytes, obj.bytes)

    }


//    @Test
//    fun packet(){
//
//        val forwarding = true
//        val accepting = true
//        val connections: List<ULong> = listOf()
//        val channels : List<String> = listOf()
//
//        var obj : Any = NodeInfo("user",forwarding,accepting,connections,channels)
//
//        val type0 : Int = MeshRaw.INFO
//        val nodeInfo : NodeInfo = obj as NodeInfo
//        val requests = null
//        val newData = null
//        val posts = null
//        val misc = null
//
//        obj = MeshRaw(type0,nodeInfo,requests,newData,posts,misc)
//
//        val type : Int = 0
//        val mid : Int = 0
//        val cur : Int = 0
//        val max : Int = 0
//        val bytes : ByteArray = Json.encodeToString(obj).toByteArray(CHARSET)
//
//        obj = Wrapper(type,mid,cur,max,bytes)
//
//        val string = Json.encodeToString(obj)
//        println(string)
//        obj = Json.decodeFromString(string) as Wrapper
//
//        println(bytes.toString(CHARSET))
//
//        //Wrapper
//        Assert.assertEquals(type, obj.type)
//        Assert.assertEquals(mid, obj.mid)
//        Assert.assertEquals(cur, obj.cur)
//        Assert.assertEquals(max, obj.max)
//        Assert.assertArrayEquals(bytes, obj.bytes)
//
//        obj = Json.decodeFromString(obj.bytes.toString(CHARSET)) as MeshRaw
//
//        //MeshRaw
//        Assert.assertEquals(type, obj.type)
//        Assert.assertEquals(nodeInfo, obj.nodeInfo)
//        Assert.assertEquals(requests, obj.requests)
//        Assert.assertEquals(newData, obj.newData)
//        Assert.assertEquals(posts, obj.posts)
//        Assert.assertArrayEquals(misc, obj.misc)
//
//        obj = obj.nodeInfo!!
//
//        //NodeInfo
//        Assert.assertEquals("user", obj.key)
//        Assert.assertEquals(forwarding, obj.fwd)
//        Assert.assertEquals(accepting, obj.accept)
//        Assert.assertEquals(connections, obj.con)
//        Assert.assertEquals(channels, obj.channels)
//
//    }

}