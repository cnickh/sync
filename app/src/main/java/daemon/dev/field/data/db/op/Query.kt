package daemon.dev.field.data.db.op

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import daemon.dev.field.MANAGER_TAG
import daemon.dev.field.data.PostRAM.getFromDB
import daemon.dev.field.data.objects.Post

@RequiresApi(Build.VERSION_CODES.O)
class Query(private val liveData : MutableLiveData<MutableList<Post>>, private val idList : List<ULong>) : Thread() {

    override fun run() {

        val postList = mutableListOf<Post>()

        for (id in idList){
            getFromDB(id.toLong())?.let { postList.add(it) }
        }


        Log.d(MANAGER_TAG, "Query Complete: $idList")
        liveData.postValue(postList)

    }


}