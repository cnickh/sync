package daemon.dev.field.util

import android.provider.Contacts.SettingsColumns.KEY
import android.util.Log
import daemon.dev.field.SERIAL_TAG
import daemon.dev.field.data.objects.*
import daemon.dev.field.nypt.Key
import daemon.dev.field.nypt.USER_KEY_SIZE
import java.net.InetAddress
import java.net.InetAddress.getByAddress


/**
 *
 * JSON? Who's she??
 *
 * */
class Serializer {

    companion object{
        const val D/*EMLIMITER*/ = '&'
        const val P/*POST DELIMITER*/ = '^'
        const val I/*INFO DELIMITER*/ = '$'
        const val U/*USER DELIMITER*/ = ':'
        const val C/*COMMENT DELIMITER*/ = '.'
    }

    val chars = Charsets.UTF_8

    fun hostToByte(host : RemoteHost) : ByteArray{
        val a = host.socketAddress?.address?.let{it.toString(chars)}
        val i = host.uid.toHex()
        val sString = a + D + i
        return sString.toByteArray(chars)
    }

    fun getHost(bytes: ByteArray) : RemoteHost {
//        Log.i(SERIAL_TAG,"Looking at host:${bytes.toString(chars)}")
        val serial = bytes.toString(chars).split(D)
        var a : InetAddress? = null
        if(serial[0]!= "null"){
            a = getByAddress(serial[0].toByteArray(chars))
        }
        val key = Key(USER_KEY_SIZE)
        key.decodeHex(serial[1])
        return RemoteHost(a,key)
    }

    fun packetToByte(packet : MeshRaw) : ByteArray{

        val t = packet.type.toString() //Int
        val i = packet.nodeInfo?.let{infoToString(it)} //DeviceInfo?
        val r = packet.requests?.joinToString(",","<",">")
        val h = packet.newData?.entries?.joinToString(",","<",">") //List<String>

        val l = mutableListOf<String>()
        packet.posts?.let {
            for (post in it) {
                l.add(postToString(post))
            }
        }

        val p = if(l.size > 0){
            l.joinToString(",")
        }else {
            null
        }


        val x = packet.extraData.toString() //ByteArray?

        val sString = t + D + i + D + r + D + h + D + p + D + x + D

        Log.i(SERIAL_TAG,"Made packet : $sString")

        val hash = sString.hashCode().toString()

        return (sString+hash+D).toByteArray(chars)
    }

    fun getPacket(bytes : ByteArray) : MeshRaw? {


        val sString = bytes.toString(chars)
        val serial = sString.split(D)

        Log.i(SERIAL_TAG,"Have raw -> $bytes")
        Log.i(SERIAL_TAG,"Have sString -> $sString")


        if(serial.size < 6){return null}

        val t = serial[0].toInt()

        val i = infoFromString(serial[1])

        val r = mutableListOf<ULong>() // List<ULong>,
        serial[2].split(',').map { it.trim('<','>') }.forEach { if(it!="null") {r.add(it.toULong())} }

        val h = mutableListOf<String>() // List<String>,
        serial[3].split(',').map { it.trim('<','>') }.forEach { if(it!="null") {h.add(it)} }
        val m = hashMapOf<ULong,String>()
        for (i in 0 until h.size){
            val item = h[i].split('=')
            if(item[0]==""){
                Log.d(SERIAL_TAG,"Error empty packet list")
                return null
            }
            m[item[0].toULong()] = item[1]
        }

        val p = mutableListOf<Post>()
            serial[4].split(',').forEach { it?.let{ string -> postFromString(string)?.let{p.add(it)}} }

        val x = serial[5].toByteArray()

        val hash = serial[6]

        return MeshRaw(t,i,r,m,p,x)
    }

    fun profileToString(profile : UserProfile) : String{

        val a = profile.alias
        val i = profile.uid.toHex()

        return a + U + i
    }

    fun profileFromString(string : String) : UserProfile? {

//        Log.i(SERIAL_TAG,"Have profile - $string")


        val serial = string.split(U)
        if(serial[0] == "null"){return null}

        val a = serial[0]
        val i = Key(0)
            i.decodeHex(serial[1])

        return UserProfile(a,i)
    }

    fun commentListToString(list : List<Comment>) : String{

        val l = mutableListOf<String>()
        list.let {
            for (comment in it) {
                l.add(commentToString(comment))
            }
        }

        return if(l.size > 0){
            transformString(l.joinToString(",","{","}"))
        }else {
            "null"
        }

    }

    fun commentListFromString(string : String) : List<Comment>{

        if(string == "null"){return mutableListOf<Comment>()}

        val list : MutableList<Comment> = mutableListOf()

        val s = transformString(string,false)

//        Log.i(SERIAL_TAG,"comment list - $s")


        if(s.length>6) {
            val serial = s.trim('{', '}').split(",")
            for (c in serial) {
                commentFromString(c)?.let { list.add(it) }
            }
        }
        return list
    }

    private fun commentToString(comment: Comment): String {

        val u = profileToString(comment.user) //UserProfile
        val c = comment.comment //String
        val t = comment.time_created.toString() //Long
        val n = comment.num.toString() //Int
        val s = comment.commentList?.let { commentListToString(it) }

        return u + C + c + C + t + C + n + C + s + C
    }

    private fun commentFromString(string : String) : Comment?{
        val serial = string.split(C)

//        Log.i(SERIAL_TAG,"comment - $string")


        if(serial[0] == "null"){return null}

        val u = profileFromString(serial[0])!! //UserProfile
        val c = serial[1] //String
        val t = serial[2].toLong() //Long
        val n = serial[3].toInt() //Int
        val s = commentListFromString(serial[4]) //List<Comment>

        val comment = Comment(u, c, t, n)

        comment.commentList = s as MutableList<Comment>

        return comment
    }

    fun postToString(post : Post) : String {
        /*Display Information*/
        val h = post.hops.toString() // Int
        val t = transformString(post.title) // String
        val b = transformString(post.body) // String
        val c = commentListToString(post.comments)

        Log.i(SERIAL_TAG, "Made comment string $c from ${post.comments}")

        val d = post.time_created.toString() // Long

        /* Network Information */
        val r = post.targets?.joinToString(",") // MutableList<String>?
        val u = profileToString(post.user) // UserProfile
        val i = post.uid // String
        val df = post.last_touched.toString() // Long

        return h + P + t + P + b + P + c + P + d + P + r + P + u + P + i + P + df + P
    }

    fun postHash(post : Post) : Int {
        val t = transformString(post.title) // String
        val b = transformString(post.body) // String
        val c = commentListToString(post.comments)

        return (t+b+c).hashCode()
    }

    fun postFromString(string: String): Post? {

//        Log.i(SERIAL_TAG,"Have post: $string")
        val serial = string.split(P)
//        Log.i(SERIAL_TAG," -> $serial")

        if(serial[0] == "null"){return null}

        /*Display Information*/
        val h = serial[0].toInt() // Int
        val t = transformString(serial[1],false) // String
        val b = transformString(serial[2],false) // String
        val c = if(serial[3] != "null"){commentListFromString(serial[3])}else{null}

        val d = serial[4].toLong() // Long

        /* Network Information */
        val r = serial[5].split(',') // MutableList<String>?
        val u = profileFromString(serial[6])!! // UserProfile
        val i = serial[7].toULong() // String
        val df = serial[8].toLong() // Long

        val post = Message(t,b,d,df,i,u)
        c?.let{
            post.comments = it as MutableList<Comment>
        }

        return post
    }

    private fun infoToString(info : NodeInfo) : String{

        val u = profileToString(info.user) // UserProfile
        val f = info.forwarding.toString() // Boolean,
        val a = info.accepting.toString() // Boolean,
        val c = info.connections?.joinToString(",","<",">") // List<ULong>,
        val b = info.public_channels?.joinToString(",","<",">") // List<String>

        val sString = u + I + f + I + a + I + c + I + b + I
        val hash = sString.hashCode().toString()

        return (sString+hash)
    }

    private fun infoFromString(string : String): NodeInfo? {
        Log.i(SERIAL_TAG,"Have info : $string")

        val serial = string.split(I)

        if(serial[0] == "null"){return null}

        val u = profileFromString(serial[0])!! // UserProfile
        val f = serial[1].toBoolean() // Boolean,
        val a = serial[2].toBoolean() // Boolean,

        val c = mutableListOf<ULong>() // List<ULong>,
        serial[3].split(',').map { it.trim('<','>') }.forEach { if(it!="null") {c.add(it.toULong())} }

        val b = mutableListOf<String>() //List<String>
        serial[4].split(',').map { it.trim('<','>') }.forEach { if(it!="null") {b.add(it)} }

        val hash = serial[5]

        return NodeInfo(u,f,a,c,b)
    }

    fun wrapperToBytes(wrapper: Wrapper) : ByteArray{

        val t = wrapper.type.toString()
        val m = wrapper.mid.toString()
        val c = wrapper.cur.toString()
        val l = wrapper.max.toString()
        val b = transformString(wrapper.bytes.toString(chars))

        val sString = t + D + m + D + c + D + l + D + b

        Log.i(SERIAL_TAG,"Made wrapper : $sString")

        return sString.toByteArray(chars)
    }

    fun bytesToWrapper(bytes : ByteArray) : Wrapper? {
//        Log.i(SERIAL_TAG,"Have wrapper : ${bytes.toString(chars)}")

        val serial = bytes.toString(chars).split(D)

        if(serial.size < 4){return null}

        val t = serial[0].toInt()
        val m = serial[1].toInt()
        val c = serial[2].toInt()
        val l = serial[3].toInt()
        val b = transformString(serial[4],false).toByteArray(chars)

        return Wrapper(t,m,c,l,b)
    }

    private fun transformString(string : String, forward : Boolean = true) : String {

        val offset = if(forward){ 47 } else { -47 }
        var newString : String = ""

        for (i in string.indices) {
            newString += (string[i].toInt()+offset).toChar()
        }

        return newString
    }


}


/**
 * device info serialization plan ?? changed this not for real
 * 512bytes
 *
 * 32 = User alias
 * 8 = User id
 * 2 = TrafficControl
 * ~50 = Connections, network-info
 * 256 = Post state information (32 posts ids (ulong))
 * ~36 + 128 = Unused, encryption??
 *
 *
 *
 * {

override fun toString(): String {

return "{"+alias + ":" + id.toString()+"}"
}

}
 *
 *
 *
 *
 *
 * 0..31 User alias
 * 32..39 User ID
 * 40..41 TrafficControl
 * 42..91 Connections, network-info
 * 92..347 Post state information (32 posts ids (ulong))
 * 348..511 ???
 *
 */