//package daemon.dev.field
//
////import daemon.dev.field.cereal.Cerealizer
//import daemon.dev.field.cereal.objects.Comment
//import daemon.dev.field.cereal.objects.UserProfile
//import daemon.dev.field.nypt.Key
//import daemon.dev.field.util.CommentMerge
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//import java.lang.Thread.sleep
//
//
//class CommentMergeTestSuite {
//
//    private val cereal = Cerealizer()
//
//    private val user0 = UserProfile("user#0", Key(8))
//    private val user1 = UserProfile("user#1", Key(8))
//
//    val thread0 = mutableListOf<Comment>()
//    val thread1 = mutableListOf<Comment>()
//
//    val expect = mutableListOf<Comment>()
//
//    private fun verify(){
//        println("thread0: \n"+cereal.humanCstring(thread0))
//        println("thread1: \n"+cereal.humanCstring(thread1))
//        println("expected: \n"+cereal.humanCstring(expect))
//        val a = cereal.commentListToString(thread0)
//        val b = cereal.commentListToString(thread1)
//        val merge = CommentMerge(a,b)
//        println("actual: \n"+cereal.humanCstring(merge.composed))
//        Assert.assertEquals(cereal.commentListToString(expect),merge.getResult())
//    }
//
//
//    private fun time() : Long{
//        return System.currentTimeMillis()
//    }
//
//    private fun hash(cmnt : Comment) : Int{
//
//        val i = cmnt.user.uid.toHex()
//        val c = cmnt.comment
//        val t = cmnt.time_created.toString()
//
//        return (i+c+t).hashCode()
//    }
//
//    @Test
//    fun merge0(){
//        val com0 = Comment(user0, "Hi",time(),0)
//        thread0.add(com0)
//        expect.add(com0)
//
//        sleep(3)
//
//        val com1 = Comment(user1, "Hey",time(),0)
//        thread1.add(com1)
//        expect.add(com1)
//
//        verify()
//    }
//
//    @Test
//    fun merge1(){
//
//        for(i in (0..4)) {
//            val sub = Comment(user0, "text$i", time(), i)
//            thread0.add(sub)
//            expect.add(sub)
//        }
//        verify()
//    }
//
//    @Test
//    fun merge2(){
//        for(i in (0..4)) {
//            val sub = Comment(user0, "text$i", time(), i)
//            thread0.add(sub)
//            expect.add(sub)
//        }
//        sleep(3)
//        for(i in (0..4)) {
//            val sub = Comment(user1, "text$i", time(), i)
//            thread1.add(sub)
//            expect.add(sub)
//        }
//
//        verify()
//
//    }
//
//    @Test
//    fun merge3(){
//
//    }
//
//    @Test
//    fun merge4(){
//        for(i in (0..4)) {
//            var sub = Comment(user0, "text$i", time(), i)
//            thread0.add(sub)
//            sub = Comment(user0, "text$i", time(), i)
//            thread1.add(sub)
//            sub = Comment(user0, "text$i", time(), i)
//            expect.add(sub)
//        }
//
//        sleep(3)
//
//        for(i in (0..4)){
//            if(i % 2 == 0){
//                val sub = Comment(user1, "sub-text$i", time(), i)
//                thread0[i].add(sub)
//                for(c in expect){
//                    if(hash(thread0[i])==hash(c)){
//                        c.add(sub)
//                    }
//                }
//            }else{
//                val sub = Comment(user0, "sub-text$i", time(), i)
//                thread1[i].add(sub)
//                for(c in expect){
//                    if(hash(thread1[i])==hash(c)){
//                        c.add(sub)
//                    }
//                }
//            }
//        }
//
//        verify()
//
//    }
//
//}