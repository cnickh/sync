package daemon.dev.field.fragments.dialogs

import daemon.dev.field.R
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import daemon.dev.field.fragments.model.DialogModel
import kotlin.math.roundToInt

class BlockDialog(var c: Activity, var dialogModel : DialogModel) : Dialog(c), View.OnClickListener {

    var d: Dialog? = null
    var yes: Button? = null
    var no: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.block_pop_up)
        yes = findViewById(R.id.block)
        no = findViewById(R.id.back)
        yes!!.setOnClickListener(this)
        no!!.setOnClickListener(this)
        beautify()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.block -> {
                dialogModel.block()
                dismiss()
            }
            R.id.back -> dismiss()
            else -> {}
        }
        dismiss()
    }

    private fun beautify() {
        findViewById<ConstraintLayout>(R.id.profile_header)
            .viewTreeObserver.addOnGlobalLayoutListener {
                val root = findViewById<ConstraintLayout>(R.id.profile_header)
                val phi = 1.618033988749895
                val params = root.layoutParams
                params.width = (root.height * phi).roundToInt()
                root.layoutParams = params
            }
    }

}

