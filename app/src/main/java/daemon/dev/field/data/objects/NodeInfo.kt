package daemon.dev.field.data.objects



class NodeInfo(val user : UserProfile,
               val forwarding : Boolean,
               val accepting : Boolean,
               val connections: List<ULong>?,
               val public_channels : List<String>?
                         )