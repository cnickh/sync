package daemon.dev.field.data.db.op

import android.os.Build
import androidx.annotation.RequiresApi
import daemon.dev.field.data.PostRAM
import daemon.dev.field.data.Server
import daemon.dev.field.network.PeerRAM

@RequiresApi(Build.VERSION_CODES.O)
class Update(val cString : String, val uid : Long) : Thread(){

    override fun run() {

        PostRAM.postDao.updateComment(cString,uid)

        val users = PeerRAM.activeUsers.value!!

        for(k in users){
            val sid = PeerRAM.getSockOfUser(k)
            sid?.let{
                PeerRAM.getServer().obtainMessage(0,
                    Server.Request(it, Server.NOTIFY, listOf(uid.toULong()),null)
                ).sendToTarget()
            }
        }

    }

}