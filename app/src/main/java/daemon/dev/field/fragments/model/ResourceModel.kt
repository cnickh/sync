package daemon.dev.field.fragments.model


import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import daemon.dev.field.data.ResTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResourceModel(private val resTable : ResTable) : ViewModel() {

    val liveProfile = MutableLiveData<Uri>(null)

    fun setProfileImage(uri : Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            liveProfile.postValue(uri)
            resTable.setProfileImage(uri.toString())
        }
    }

    fun loadProfileImage(){
        viewModelScope.launch(Dispatchers.IO) {
            resTable.getProfileImage()?.let{
                val uri = it.toUri()
                liveProfile.postValue(uri)
            }
        }
    }

    fun getUserProfile(key : String) : LiveData<String?> {
        return resTable.getResource(key)
    }

}