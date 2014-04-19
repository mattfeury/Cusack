package com.mattfeury.cusack.modules

import com.mattfeury.cusack.CusackReceiver
import com.mattfeury.cusack.R

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewManager
import android.widget.TextView

class SongInfoModule[A <: CusackReceiver with Context](receiver:A, attrs:AttributeSet = null) extends Module(receiver, attrs) {
    override def selected = {}

    override def render(view:View) = {
        val textView = view.findViewById(R.id.moduleText).asInstanceOf[TextView]
        val moduleImage = view.findViewById(R.id.moduleImage)

        textView.setTextAppearance(receiver, R.style.boldText)
        moduleImage.getParent.asInstanceOf[ViewManager].removeView(moduleImage)

        for {
            song <- currentSong
            artistName = song.artist.name
            songName = song.name
            albumName = song.album
        } {
            val verb = artistName match {
                case artist if artist.matches("""(?i)the .*s""") => "are"
                case _ => "is"
            }

            textView.setText(s"$artistName $verb playing $songName from the album $albumName")
        }
    }
}