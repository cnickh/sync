package daemon.dev.field.data.objects

import android.net.Uri

data class Media(override val title: String,
                   override val body: String,
                   override val time_created: Long,
                   override var last_touched: Long,
                   override val uid: ULong,
                   override val user : UserProfile,
                   override var uri : Uri?
) : Post {

    override var hops: Int = 0
    override var comments = mutableListOf<Comment>()
    override val targets = mutableListOf<String>()

}