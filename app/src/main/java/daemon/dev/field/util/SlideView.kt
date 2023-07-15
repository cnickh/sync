package daemon.dev.field.util

import android.util.Log
import android.view.MotionEvent
import android.view.View


class SlideView(val slideView : View) : View.OnTouchListener {

    var initialX = 0F
    var startX = 0F

    override fun onTouch(view: View, event: MotionEvent): Boolean {

        val params = slideView.layoutParams
        var delta = 0

        Log.d("OnTouch","ParamsHeight = ${params.height}")

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                startX = event.rawX
                initialX = event.rawX
                Log.v("OnTouch"," touch @ $initialX")
            }

            MotionEvent.ACTION_MOVE -> {
                delta = (initialX - event.rawY).toInt()
                initialX = event.rawY

                //if( (delta + params.height) >= 168 && (delta + params.height) <= 1330){
                    params.width -= delta
                    slideView.layoutParams = params
               // }

                Log.v("OnMove","@ ${event.rawY}")

            }

            MotionEvent.ACTION_UP -> {
//                if(event.rawY == startY){
//
//                    if(params.height > 168){
//                        params.height = 168
//                        slideView.layoutParams = params
//                    } else {
//                        params.height = 1330
//                        slideView.layoutParams = params
//                    }
//
//                }
            }

            else -> return false
        }
        return true
    }

}
