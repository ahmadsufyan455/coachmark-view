package com.zerodev.coachmark_view

import android.content.Context
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

class DefaultCoachmarkView(context: Context) : ConstraintLayout(context) {
    private var titleText = ""
    private var buttonText = ""
    private var onButtonClicked: (() -> Unit)? = null

    init {
        inflate(context, R.layout.coachmark_view_default, this)
    }

    fun setTitleText(text: String) {
        val titleTextView = findViewById<TextView>(R.id.tv_title)
        titleText = text
        titleTextView.text = titleText
    }

    fun setButtonText(text: String) {
        val buttonView = findViewById<Button>(R.id.bt_main)
        buttonText = text
        buttonView.text = buttonText
    }

    fun setOnButtonClickedHof(onClicked: () -> Unit) {
        val buttonView = findViewById<Button>(R.id.bt_main)
        onButtonClicked = onClicked
        onButtonClicked?.let {
            buttonView.setOnClickListener {
                it()
            }
        }
    }

    class Builder(private val context: Context) {
        private var titleText: String = ""
        private var buttonText: String = ""
        private var onButtonClicked: (() -> Unit)? = null

        fun setTitleText(text: String) {
            titleText = text
        }

        fun setButtonText(text: String) {
            buttonText = text
        }

        fun setOnButtonClickedHof(onClicked: () -> Unit) {
            onButtonClicked = onClicked
        }

        fun build(): DefaultCoachmarkView {
            val defaultCoachmarkView = DefaultCoachmarkView(context)
            defaultCoachmarkView.setTitleText(titleText)
            defaultCoachmarkView.setButtonText(buttonText)
            onButtonClicked?.let { defaultCoachmarkView.setOnButtonClickedHof(it) }
            return defaultCoachmarkView
        }
    }
}