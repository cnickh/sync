package daemon.dev.field.data.objects

import android.net.Uri

data class Message(override val title: String,
                   override val body: String,
                   override val time_created: Long,
                   override var last_touched: Long,
                   override val uid: ULong,
                   override val user : UserProfile
) : Post {

    override var hops: Int = 0
    override var comments = mutableListOf<Comment>()
    override val targets = mutableListOf<String>()
    override var uri : Uri? = null



}