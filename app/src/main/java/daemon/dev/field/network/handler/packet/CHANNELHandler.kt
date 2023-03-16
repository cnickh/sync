package daemon.dev.field.network.handler.packet

import daemon.dev.field.CHARSET
import daemon.dev.field.data.ChannelAccess

class CHANNELHandler(val channelAccess : ChannelAccess) {

    suspend fun handle(string : String){
        val selected = string.split(",")

        for(c in selected){

            channelAccess.createChannel(c,"null")

        }

    }

}