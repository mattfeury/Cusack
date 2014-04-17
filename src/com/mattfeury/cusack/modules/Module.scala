package com.mattfeury.cusack.modules

import com.mattfeury.cusack.CusackReceiver
import com.mattfeury.cusack.music.Song
import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.mattfeury.cusack.R
import android.widget.ImageView

abstract class Module[A <: CusackReceiver with Context](receiver:A, attrs:AttributeSet) {
    var currentSong:Option[Song] = None

    def logo:Option[Int] = None
    val templateId = R.layout.module_view

    def songChanged(song:Song) {
        currentSong = Some(song)
    }

    def render(view:View)

    def renderLogo(view:View) = {
        logo match {
            case Some(resource) =>
                    val moduleImage = view.findViewById(R.id.moduleImage).asInstanceOf[ImageView]
                    val icon = receiver.getResources().getDrawable(resource)
                    moduleImage.setImageDrawable(icon)

            case _ =>
        }
    }

    //def expanded
    //def collapsed()
    def selected()
}