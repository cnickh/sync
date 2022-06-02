package daemon.dev.field.data.db.op

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import daemon.dev.field.DB_OP
import daemon.dev.field.MANAGER_TAG
import daemon.dev.field.data.PostRAM
import daemon.dev.field.data.Server
import daemon.dev.field.data.db.PostEntity
import daemon.dev.field.data.objects.Post
import daemon.dev.field.network.PeerRAM
import daemon.dev.field.util.CommentMerge
import daemon.dev.field.util.Serializer

@RequiresApi(Build.VERSION_CODES.O)
class Request(private val postList : List<Post>) : Thread() {

    override fun run(){

        for(post in postList){
            val up = Serializer().postHash(post)
            Log.i(DB_OP,"updating with : $up")

            val original = PostRAM.postDao.get(post.uid.toLong())

            if(original==null) {
                Log.i(DB_OP,"Storing post : ${post.uid}")
                val entity =
                    PostEntity(
                        post.hops, post.title, post.body,
                        Serializer().commentListToString(post.comments), "null",
                        post.uid.toLong(), Serializer().profileToString(post.user),
                        post.time_created, post.last_touched
                    )

                PostRAM.postDao.insert(entity)

            } else {
                Log.i(DB_OP,"Updating comment on post : ${post.uid}")

                val cmnt0 = original.cString

                /**Fix me please this is terrible*/
                val cmnt1 = Serializer().commentListToString(post.comments)

                val merge = CommentMerge(cmnt0,cmnt1)

                PostRAM.postDao.updateComment(
                    merge.getResult(),
                    post.uid.toLong()
                )

                PostRAM.newChat.postValue(post.uid)

            }

            val hash = PostRAM.getFromDB(post.uid.toLong())?.let { Serializer().postHash(it) }
            Log.i(DB_OP,"new hash after : $hash")


            val users = PeerRAM.activeUsers.value!!

            for(k in users){
                val sid = PeerRAM.getSockOfUser(k)
                sid?.let{
                    PeerRAM.getServer().obtainMessage(0,
                        Server.Request(it, Server.NOTIFY, listOf(post.uid),null)
                    ).sendToTarget()
                }
            }

        }

        PostRAM.updatePost()
    }

}