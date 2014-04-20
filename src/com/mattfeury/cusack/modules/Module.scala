package com.mattfeury.cusack.modules

import com.mattfeury.cusack.CusackReceiver
import com.mattfeury.cusack.R
import com.mattfeury.cusack.music.Song
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mattfeury.cusack.analytics.Mixpanel

abstract class Module[A <: CusackReceiver with Context](receiver:A, attrs:AttributeSet) {
    var currentSong:Option[Song] = None

    def logo:Option[Int] = None
    val templateId = R.layout.module_view

    def songChanged(song:Song) {
        currentSong = Some(song)
    }

    def onRender(view:View) = {
        renderLogo(view)
        render(view)
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

    def redraw() {
        receiver.redraw()
    }

    def onSelect() = {
        selected()

        Mixpanel.track("Module selected", Map(
            ("class" -> this.getClass().getSimpleName())
        ) ++ currentSong.map(_.toMap).getOrElse(Map()))
    }

    // Override these if you so choose
    def selected() = {}
}

trait Expandable[T <: CusackReceiver with Context] extends Module[T] {
    var isExpanded = false

    def allowCollapse() = true

    def toggle() = {
        if (isExpanded) {
            collapse()
        } else {
            expand()
        }
    }

    def expand() = {
        isExpanded = true
        expanded()
        redraw()
    }

    def collapse() : Unit = {
        if (! allowCollapse) {
            return
        }

        isExpanded = false
        collapsed()
        redraw()
    }

    def expanded() = {}
    def collapsed() = {}

    override def onRender(view:View) = {
        super.onRender(view)

        val moduleText = view.findViewById(R.id.moduleText).asInstanceOf[TextView]

        if (isExpanded) {
            moduleText.setMaxLines(Integer.MAX_VALUE)
        } else if (allowCollapse()) {
            moduleText.setMaxLines(3)
        }

    }
}
