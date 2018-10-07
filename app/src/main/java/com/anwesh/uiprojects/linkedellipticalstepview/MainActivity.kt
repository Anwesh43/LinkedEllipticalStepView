package com.anwesh.uiprojects.linkedellipticalstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.ellipticalstepview.EllipticalStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EllipticalStepView.create(this)
    }
}
