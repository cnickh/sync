package daemon.dev.field.data.objects

class Comment(val user:UserProfile, val comment:String, var time_created:Long, val num:Int){

    var commentList : MutableList<Comment> = mutableListOf()

}