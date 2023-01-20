package daemon.dev.field.fragments.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DialogModel  : ViewModel() {

    val blockedStatus = MutableLiveData(true)

    val selectedChannels = mutableListOf<String>()

    fun selectChannel(id : String) : Boolean{
        return if(selectedChannels.contains(id)){
            selectedChannels.remove(id)
            false
        } else {
            selectedChannels.add(id)
            true
        }
    }

    fun clearSelection(){
        selectedChannels.clear()
    }

    fun useSelected(){
        //send selected Channels to peer
        clearSelection()
    }

    fun block(){
        blockedStatus.postValue(false)
    }

    fun unBlock(){
        blockedStatus.postValue(true)
    }

}