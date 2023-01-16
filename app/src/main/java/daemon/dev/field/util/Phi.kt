package daemon.dev.field.util

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.roundToInt


class Phi {

    fun phi(width : Int, lvl : Int) : Int{

        val phi = 1.618033988749895

        val res = (width*phi) / (lvl*phi + 1)

        return res.roundToInt()

    }

    fun pxToDp(px : Int, context : Context) : Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return (px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

}