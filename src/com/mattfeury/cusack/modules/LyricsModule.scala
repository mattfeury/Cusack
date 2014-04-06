package com.mattfeury.cusack.modules

import com.mattfeury.cusack.CusackReceiver
import com.mattfeury.cusack.R
import com.mattfeury.cusack.Song

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView

class LyricsModule[A <: CusackReceiver with Context](receiver:A, attrs:AttributeSet = null) extends Module(receiver, attrs) {
    override def selected = {
        currentSong.foreach(sendToRapGenius _)
    }

    def sendToRapGenius(song:Song) = {
        // TODO urlEncode
        receiver.openURIIntent("http://rapgenius.com/search?q=" + song.artist + " " + song.name)
    }

    override def render(view:View) = {
        val moduleText = view.findViewById(R.id.moduleText).asInstanceOf[TextView]
        moduleText.setText("Touch for lyrics")
    }
}