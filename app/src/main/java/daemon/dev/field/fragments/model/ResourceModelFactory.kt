package daemon.dev.field.fragments.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import daemon.dev.field.data.ResTable
import daemon.dev.field.data.db.SyncDatabase
import java.util.*

class ResourceModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResourceModel::class.java)) {

            val sync = SyncDatabase.getInstance(context)

            val resDao = sync.resDao

            return ResourceModel(
                ResTable(resDao)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}