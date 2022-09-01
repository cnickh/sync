package daemon.dev.field.cereal.objects

data class Address (val address : String){
    fun key() : String = address.split(":")[0]
    fun time() : Long = address.split(":")[1].toLong()
}