package daemon.dev.field.network.handler.packet

import daemon.dev.field.data.ChannelAccess

class CHANNELHandler(val channelAccess : ChannelAccess) {

    suspend fun handle(string : String,key : String){
        val selected = string.split(",")

        for(c in selected){
            channelAccess.createChannel(c,"shared:$key")
        }

    }

}