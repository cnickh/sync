package daemon.dev.field.fragments.dialogs

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import daemon.dev.field.R
import daemon.dev.field.fragments.model.SyncModel
import kotlin.math.roundToInt

class CreateDialog(var c: Activity, var syncModel : SyncModel) : Dialog(c), View.OnClickListener {

    var d: Dialog? = null
    var yes: Button? = null
    var no: Button? = null
    var editText : EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.create_channel)
        yes = findViewById(R.id.create)
        no = findViewById(R.id.cancel)
        editText = findViewById(R.id.channel_id)

        yes!!.setOnClickListener(this)
        no!!.setOnClickListener(this)
        beautify()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.create -> {
                val text = editText!!.text.toString()
                if(text != ""){
                    syncModel.addChannel(editText!!.text.toString())
                }
                dismiss()
            }
            R.id.cancel -> dismiss()
            else -> {}
        }
        dismiss()
    }

    private fun beautify() {
        findViewById<ConstraintLayout>(R.id.profile_header)
            .viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    val root = findViewById<ConstraintLayout>(R.id.profile_header)
                    val phi = 1.618033988749895
                    val params = root.layoutParams
                    params.width = (root.height * phi).roundToInt()
                    root.layoutParams = params

                }
            })
    }

}
