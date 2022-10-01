package daemon.dev.field.data

import androidx.lifecycle.LiveData
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.data.db.UserDao

class UserBase(private val sync : UserDao) {

    val users = sync.getAll()

    suspend fun add(user : User){
        sync.insert(user)
    }

    suspend fun update(user : User){
        sync.update(user)
    }

    suspend fun wait(key : String) : User?{
        return sync.wait(key)
    }

    suspend fun addChannel(string : String){
        val ch : MutableList<String>
        var channels = sync.getChannels()
        if(channels == "null"){
            channels = string
        }else{
            ch = channels.split(',') as MutableList<String>
            ch.add(string)
            channels = ch.joinToString(",")
        }
        sync.setChannel(channels)
    }

    suspend fun getChannels() : List<String>{
        return sync.getChannels().split(',')
    }

    fun getUsers(list : List<String>) : LiveData<List<User>>{
        return sync.getUsers(list)
    }

    fun getUser(key : String) : LiveData<User> {
        return sync.get(key)
    }


}