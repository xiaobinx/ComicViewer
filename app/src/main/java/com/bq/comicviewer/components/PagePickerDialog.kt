package com.bq.comicviewer.components

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.bq.comicviewer.R
import kotlinx.android.synthetic.main.dialog_page_picker.*
import kotlin.concurrent.thread

class PagePickerDialog(context: Context, val action: (PagePickerDialog, Int) -> Boolean) : Dialog(context) {

    var maxp: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_page_picker)
        editText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = v.text.toString()
                if (text.isNotEmpty()) {
                    action(this, text.toInt()).apply {
                        if (!this) {
                            dismiss()
                        }
                    }
                } else {
                    true
                }

            } else true
        }
    }

    @SuppressLint("SetTextI18n")
    override fun show() {
        super.show()
        textView.text = "/$maxp"
        editText.apply {
            text = null
            requestFocus()
            val imm = (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            thread {
                Thread.sleep(100)
                imm.showSoftInput(this, 0)
            }
        }
    }

    fun setEditText(i: Int) {
        editText.setText(i.toString())
    }
}