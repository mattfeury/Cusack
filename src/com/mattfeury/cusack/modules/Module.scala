package com.mattfeury.cusack.modules

import com.mattfeury.cusack.CusackReceiver
import com.mattfeury.cusack.music.Song
import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.mattfeury.cusack.R

abstract class Module[A <: CusackReceiver with Context](receiver:A, attrs:AttributeSet) {
    var currentSong:Option[Song] = None

    val templateId = R.layout.module_view

    def songChanged(song:Song) {
        currentSong = Some(song)
    }

    def render(view:View)

    //def expanded
    //def collapsed()
    def selected()
}