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
import android.view.ViewManager
import com.mattfeury.cusack.util.Utils


class WikipediaModule[A <: CusackReceiver with Context](receiver:A, attrs:AttributeSet = null) extends Module(receiver, attrs) with Expandable[A] {
    override def logo = Some(R.drawable.wikipedia)

    // always expanded for small single paragraphs
    override def allowCollapse() = getTextToShow().length() > 320

    override def selected = {
        for {
            song <- currentSong
            artist = song.artist
        } {
            // If no wikipedia article, click to google search
            val url = artist.wikipediaPageInfo.map { _.getUrl() } getOrElse {
                "https://www.google.com/search?" + Utils.makeQueryString(Map("q" -> song.artist.name))
            }
            receiver.openURIIntent(url)
        }
    }

    // If there is no wiki, then this shouldn't really be in expandable mode, so we trigger selected() on expansion toggle
    // This is kinda a hack.
    override def toggle = {
        super.toggle()

        currentSong.flatMap(_.artist.wikipediaPageInfo.map(_.extract)) match {
            case None => selected()
            case _ =>
        }
    }

    override def render(view:View) = {
        val moduleText = view.findViewById(R.id.moduleText).asInstanceOf[TextView]
        moduleText.setText(getTextToShow())
    }

    private def getTextToShow() = {
        {
            for {
                song <- currentSong
                artist = song.artist
                extract = artist.wikipediaPageInfo.map(_.extract).getOrElse("Search for artist info")
            } yield {
                extract
            }
        } getOrElse "-"
    }
}