package daemon.dev.field.cereal.objects

import kotlinx.serialization.Serializable

@Serializable
data class HandShake(val state : Int, val me : User, val peer : List<User>?, var keyBundle : KeyBundle?)