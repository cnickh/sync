package daemon.dev.field.fragments.dialogs

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import daemon.dev.field.R
import daemon.dev.field.fragments.model.SyncModel
import daemon.dev.field.util.Phi
import kotlin.math.roundToInt

class JoinDialog(var c: Activity, var syncModel : SyncModel, val name : String) : Dialog(c), View.OnClickListener {

    var d: Dialog? = null
    var yes: Button? = null
    var no: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.join_dialog)
        yes = findViewById(R.id.join)
        no = findViewById(R.id.cancel)

        yes!!.setOnClickListener(this)
        no!!.setOnClickListener(this)
        beautify()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.join -> {
                syncModel.buildChannel(name)
                dismiss()
            }
            R.id.cancel ->{
                syncModel.removeChannel(name)
                dismiss()
            }
            else -> {}
        }
    }

    private fun beautify() {
        findViewById<ConstraintLayout>(R.id.profile_header)
            .viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    findViewById<ConstraintLayout>(R.id.profile_header)!!.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val root = findViewById<ConstraintLayout>(R.id.profile_header)
                    val params = root.layoutParams
                    params.height = Phi().phi(root.width,1)
                    root.layoutParams = params

                }
            })
    }

}
