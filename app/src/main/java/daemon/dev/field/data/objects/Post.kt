package daemon.dev.field.data.objects

import android.net.Uri


interface Post{

    /*Display Information*/
    var hops : Int
    val title : String
    val body : String
    var comments : MutableList<Comment>
    val time_created : Long
    var uri : Uri?

    /* Network Information */
    val targets : MutableList<String>?
    val user : UserProfile
    val uid : ULong
    var last_touched: Long


}