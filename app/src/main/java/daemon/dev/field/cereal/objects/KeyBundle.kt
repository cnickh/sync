package daemon.dev.field.cereal.objects

import kotlinx.serialization.Serializable

@Serializable
data class KeyBundle (val secret : String, val sig : String)