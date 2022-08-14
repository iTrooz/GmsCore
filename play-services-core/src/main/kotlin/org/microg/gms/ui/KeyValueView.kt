package org.microg.gms.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import android.view.LayoutInflater
import android.content.res.TypedArray
import android.util.AttributeSet
import com.google.android.gms.R
import org.microg.gms.common.Utils

class KeyValueView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    init{
        LayoutInflater.from(context).inflate(R.layout.key_value, this, true)
    }
    private val keyView: TextView = findViewById(R.id.key)
    private val valueView: TextView = findViewById(R.id.value)

    fun setValueText(text: CharSequence) {
        valueView.text = text
    }
    fun setValueColor(color: Int) {
        valueView.setTextColor(color)
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.KeyValueView, 0, 0).let {
            keyView.text = it.getString(R.styleable.KeyValueView_key)
        }
    }
}