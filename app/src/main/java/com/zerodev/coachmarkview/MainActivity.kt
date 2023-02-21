package com.zerodev.coachmarkview

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zerodev.coachmark_view.CoachmarkView
import com.zerodev.coachmark_view.DefaultCoachmarkView
import com.zerodev.coachmark_view.config.DismissType
import com.zerodev.coachmark_view.config.Gravity
import com.zerodev.coachmark_view.config.PointerType

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvHello = findViewById<TextView>(R.id.tv_hello)

        // example usage
        createCoachmark(tvHello)
    }

    // example function
    private fun createCoachmark(view: View) {
        var coachmarkView: CoachmarkView? = null
        val defaultCoachmarkView = DefaultCoachmarkView.Builder(this).apply {
            setButtonText("Ok, got it")
            setTitleText("Complete your information here.")
            setOnButtonClickedHof {
                coachmarkView?.dismiss()
            }
        }.build()
        coachmarkView = CoachmarkView.Builder(this)
            .setGravity(Gravity.auto) //optional
            .setPointerType(PointerType.arrow)
            .setDismissType(DismissType.anywhere) //optional - default DismissType.targetView
            .setTargetView(view)
            .setMessageView(defaultCoachmarkView)
            .build()

        coachmarkView.show()
    }
}