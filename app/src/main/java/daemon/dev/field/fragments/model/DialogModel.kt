package daemon.dev.field.fragments.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import daemon.dev.field.cereal.objects.MeshRaw
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DialogModel  : ViewModel() {

    val blockedStatus = MutableLiveData(true)

    private val selectedChannels = mutableListOf<String>()

    var key : String? = null

    fun setUser(key : String){
        Log.d("DialogModel.kt","Setting key $key")
        this.key = key
    }

    private fun clearUser(){
        Log.d("DialogModel.kt","Clearing key")
        this.key = null
    }

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
        clearUser()
        selectedChannels.clear()
    }

    fun useSelected(){
        //send selected Channels to peer
        val selected = selectedChannels
            .joinToString(",")

        val raw =
            MeshRaw(MeshRaw.CHANNEL,
                null,null,null,null,selected)

        viewModelScope.launch(Dispatchers.IO) {
            Log.d("DialogModel.kt","Sending channel to $key")
            //Sync.queue(key!!, raw)
            clearSelection()
        }
    }

    fun block(){
        clearUser()
        blockedStatus.postValue(false)
    }

    fun unBlock(){
        clearUser()
        blockedStatus.postValue(true)
    }

}