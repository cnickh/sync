package daemon.dev.field.fragments.dialogs

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import daemon.dev.field.R
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.fragments.model.SyncModel
import daemon.dev.field.util.Phi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

class JoinDialog(val view : View, var c: Activity, var syncModel : SyncModel, val name : String, val user : LiveData<User>) : Dialog(c), View.OnClickListener {

    var d: Dialog? = null
    var yes: Button? = null
    var no: Button? = null
    var share: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.join_dialog)
        yes = findViewById(R.id.join)
        no = findViewById(R.id.cancel)
        share = findViewById(R.id.shared)

        yes!!.setOnClickListener(this)
        no!!.setOnClickListener(this)
        beautify()

        view.findViewTreeLifecycleOwner()?.let {
            user.observe(it) { user ->

                Log.i("JoinDialog.kt","Join pulled user ${Json.encodeToString(it)}")

                share?.let{ textView ->
                    val start = "Shared by: \n"
                    user?.let{ prof ->
                        textView.text = start + prof.alias
                    }
                }

            }
        }

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
