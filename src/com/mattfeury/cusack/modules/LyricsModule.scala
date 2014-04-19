package com.mattfeury.cusack.modules

import com.mattfeury.cusack.CusackReceiver
import com.mattfeury.cusack.R
import com.mattfeury.cusack.music.Song
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.mattfeury.cusack.util.Utils

class LyricsModule[A <: CusackReceiver with Context](receiver:A, attrs:AttributeSet = null) extends Module(receiver, attrs) {

    override def logo = Some(R.drawable.mic)

    override def selected = {
        currentSong.foreach(sendToRapGenius _)
    }

    def sendToRapGenius(song:Song) = {
        val params = Utils.makeQueryString(Map(("q", song.artist.name + " " + song.name)))
        receiver.openURIIntent("http://rapgenius.com/search?" + params)
    }

    override def render(view:View) = {
        val moduleText = view.findViewById(R.id.moduleText).asInstanceOf[TextView]
        val text = currentSong.map(_ => "Tap to search RapGenius").getOrElse("-")
        moduleText.setText(text)
    }
}