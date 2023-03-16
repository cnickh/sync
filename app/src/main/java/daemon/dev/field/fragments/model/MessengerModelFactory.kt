package daemon.dev.field.fragments.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MessengerModelFactory() : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessengerModel::class.java)) {
            return MessengerModel(
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}