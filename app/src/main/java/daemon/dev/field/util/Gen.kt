package daemon.dev.field.util

import daemon.dev.field.CHARSET
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.cereal.objects.Post
import java.text.SimpleDateFormat
import java.util.*

class Gen {

fun genPost() : List<Post>{

    val list = mutableListOf<Post>()

        list.add(
            Post(
                PUBLIC_KEY.toString(CHARSET), System.currentTimeMillis(),
                "Example title",
                "Random Conent",
                "null",
                13, 0,
            ))

    list.add(
        Post(
            PUBLIC_KEY.toString(CHARSET), System.currentTimeMillis(),
            "Example title",
            "Random Conent",
            "null",
            5, 0,
        ))

    list.add(
        Post(
            PUBLIC_KEY.toString(CHARSET), System.currentTimeMillis(),
            "Example title",
            "Yep, a lot of these buildings in SF / Bay Area have popped up in the last 10 or less. I went to school downtown SF and across market at 8th was a shitty apartment building. In the last few years it’s turned into a nice high rise with a Whole Foods on the first floor ",
            "null",
            67, 0,
        ))

    list.add(
        Post(
            PUBLIC_KEY.toString(CHARSET), System.currentTimeMillis(),
            "Example title",
            "Random Conent",
            "null",
            103, 0,
        ))

    list.add(
        Post(
            PUBLIC_KEY.toString(CHARSET), System.currentTimeMillis(),
            "Example title",
            "Random Conent",
            "null",
            66, 0,
        ))

    list.add(
        Post(
            PUBLIC_KEY.toString(CHARSET), System.currentTimeMillis(),
            "Example title",
            "Random Conent",
            "null",
            2, 0,
        ))
    return list
}

    fun getDateTime(s: Long): String? {
        return try {
            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
            val netDate = Date(s)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

}