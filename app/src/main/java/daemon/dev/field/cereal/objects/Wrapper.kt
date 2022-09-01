package daemon.dev.field.cereal.objects

import kotlinx.serialization.Serializable

@Serializable
class Wrapper(
                val type : Int,
                val mid : Int,
                val cur : Int,
                val max : Int,
                val bytes : String

               )