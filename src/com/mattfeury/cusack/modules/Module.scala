package com.mattfeury.cusack.modules

import com.mattfeury.cusack.CusackReceiver
import com.mattfeury.cusack.music.Song

import android.content.Context
import android.util.AttributeSet
import android.view.View

abstract class Module[A <: CusackReceiver with Context](receiver:A, attrs:AttributeSet) {
    var currentSong:Option[Song] = None

    def songChanged(song:Song) {
        currentSong = Some(song)
    }

    def render(view:View)

    //def expanded
    //def collapsed()
    def selected()
}