package daemon.dev.field.fragments.model

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import daemon.dev.field.data.PostRAM

class TempPost : ViewModel() {

    private var body : String? = null
    private var subject : String? = null
    private var targets : List<String>? = null


    fun setSubject(subject : String?){
        this.subject = subject
    }

    fun setBody(body : String?){
        this.body = body
    }

    fun setTargets(targets : List<String>?){
        this.targets = targets
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun create() {
        PostRAM.createMyPost(subject!!,body!!,targets!!,null)
    }

    fun clear() {
        this.subject = null
        this.body = null
        this.targets = null
    }

}