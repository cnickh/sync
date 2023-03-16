package daemon.dev.field.network.handler.packet

import androidx.lifecycle.MutableLiveData
import daemon.dev.field.CHARSET

class DIRECTHandler(val liveMsg : MutableLiveData<String>) {

    fun handle(json : String){

        liveMsg.postValue(json)

    }

}