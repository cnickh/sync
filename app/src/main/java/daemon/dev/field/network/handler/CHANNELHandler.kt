package daemon.dev.field.network.handler

import android.content.Context
import daemon.dev.field.data.ChannelAccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class CHANNELHandler(val channelAccess : ChannelAccess) {

    fun handle(string : String,key : String){
        val selected = string.split(",")
        CoroutineScope(Dispatchers.IO).launch {
            for (c in selected) {
                channelAccess.createChannel(c, "shared:$key")
            }
        }
    }

}