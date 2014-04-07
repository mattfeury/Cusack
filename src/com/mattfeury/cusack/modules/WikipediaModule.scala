package com.mattfeury.cusack.modules

import com.mattfeury.cusack.CusackReceiver
import com.mattfeury.cusack.R
import com.mattfeury.cusack.music.Song
import com.mattfeury.cusack.services.WikipediaService
import android.content.Context
import android.os.AsyncTask
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import com.mattfeury.cusack.services.WikipediaPageInfo


class WikipediaModule[A <: CusackReceiver with Context](receiver:A, attrs:AttributeSet = null) extends Module(receiver, attrs) {
    override def selected = {
        for {
            song <- currentSong
            artist = song.artist
            wikiInfo <- artist.wikipediaPageInfo
        } {
            receiver.openURIIntent(wikiInfo.getUrl())
        }
    }

    override def render(view:View) = {
        val moduleText = view.findViewById(R.id.moduleText).asInstanceOf[TextView]

        val text = for {
            song <- currentSong
            artist = song.artist
            wikiInfo <- artist.wikipediaPageInfo
        } yield {
            wikiInfo.extract
        }

        moduleText.setText(text.getOrElse(""))
    }
}