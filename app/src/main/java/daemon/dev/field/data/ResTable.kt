package daemon.dev.field.data

import androidx.lifecycle.LiveData
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.cereal.objects.Resource
import daemon.dev.field.data.db.ResDao
import java.util.*

class ResTable(private val sync : ResDao) {


    suspend fun setProfileImage(uri : String){
        val res = Resource(PUBLIC_KEY.toBase64(),uri)
        sync.insert(res)
    }

    suspend fun getProfileImage() : String?{
        return sync.get(PUBLIC_KEY.toBase64())?.uri
    }

    fun getResource(key : String) : LiveData<String?>{
        return sync.getResource(key)
    }

    private fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
    }
}