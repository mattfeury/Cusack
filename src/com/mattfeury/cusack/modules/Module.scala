package com.mattfeury.cusack.modules

import com.mattfeury.cusack.CusackReceiver
import com.mattfeury.cusack.Song

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

abstract class Module[A <: CusackReceiver with Context](receiver:A, attrs:AttributeSet) extends View(receiver, attrs) {
    var currentSong:Option[Song] = None

    def songChanged(song:Song) {
        currentSong = Some(song)
    }

    def render(view:View)

    //def expanded
    //def collapsed()
    def selected()

    override def onTouchEvent(event:MotionEvent) : Boolean = {
        event.getAction() match {
            case MotionEvent.ACTION_DOWN => selected() 
        }

        true
    }
}