package daemon.dev.field.fragments.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import daemon.dev.field.R
import daemon.dev.field.fragments.adapter.ShareAdapter
import daemon.dev.field.fragments.model.DialogModel
import java.util.*
import kotlin.collections.ArrayList


class ShareDialog(var c: Activity, var dialogModel : DialogModel, var channels : ArrayList<String>) : Dialog(c), View.OnClickListener {

    var d: Dialog? = null
    var yes: Button? = null
    var no: Button? = null
    var list : RecyclerView? = null
    private lateinit var shareAdapter : ShareAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.share_pop_up)

        yes = findViewById(R.id.share)
        no = findViewById(R.id.cancel)
        list = findViewById(R.id.channel_list)
        yes!!.setOnClickListener(this)
        no!!.setOnClickListener(this)

        shareAdapter = ShareAdapter(channels,dialogModel)

        list!!.adapter = shareAdapter
        list!!.layoutManager = GridLayoutManager(c,2)

        val root : ConstraintLayout = findViewById(R.id.root)
        root.layoutParams.width = 1000

        val editText = findViewById<EditText>(R.id.channel_search)
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                filter(s.toString())
            }
        })

        editText.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = c.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(root.windowToken, 0)
                return@OnEditorActionListener true
            }
            false
        })


    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.share -> {
                //get selected
                Log.d("AddDialog.kt","Have ${dialogModel.key}")
                dialogModel.useSelected()
                dismiss()
            }
            R.id.cancel -> {
                //clear selected
                dialogModel.clearSelection()
                dismiss()
            }
            else -> {}
        }
        dismiss()
    }

    private fun filter(text: String) {
        val filteredList: ArrayList<String> = ArrayList()
        for (item in channels) {
            if (item.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filteredList.add(item)
            }
        }
        shareAdapter.filterList(filteredList)
    }

}