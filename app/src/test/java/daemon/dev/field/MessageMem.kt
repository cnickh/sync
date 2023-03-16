package daemon.dev.field

import daemon.dev.field.cereal.objects.Comment
import daemon.dev.field.fragments.model.MessengerModel
import org.junit.Test

class MessageMem {

    @Test
    fun createComment(){
        val model = MessengerModel()

        val key0 = "key0"
        val key1 = "key1"
        val key2 = "key2"
        val key3 = "key3"

        model.createSub(key0)

        val msg0 = Comment(key0,"hi",System.currentTimeMillis())
        val msg1 = Comment(key0,"hello",System.currentTimeMillis())
        val msg2 = Comment(key3,"bird",System.currentTimeMillis())
        val msg3 = Comment(key2,"turd",System.currentTimeMillis())

        model.receiveMessage(msg0)

        model.printMsgMap()

    }

}