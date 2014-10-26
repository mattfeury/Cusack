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
import com.mattfeury.cusack.music.WikipediaKnowledgable


abstract class WikipediaModule[A <: CusackReceiver with Context](receiver:A, attrs:AttributeSet = null) extends Module(receiver, attrs) with Expandable[A] {
    def wikipediaKnowledge : Option[WikipediaKnowledgable]
    def emptyStateText : String

    override def logo = Some(R.drawable.wikipedia)

    def wikipediaInfo : Option[WikipediaPageInfo] = wikipediaKnowledge.flatMap(_.wikipediaPageInfo)

    // always expanded for small single paragraphs
    override def allowCollapse() = getTextToShow().length() > 320

    override def selected = {
        for {
            song <- currentSong
            wikipediaKnowledge <- wikipediaKnowledge
        } {
            // If no wikipedia article, click to google search
            val url = wikipediaInfo.map { _.getUrl() } getOrElse {
                "https://www.google.com/search?" + Utils.makeQueryString(Map("q" -> wikipediaKnowledge.name))
            }
            receiver.openURIIntent(url)
        }
    }

    override def toggle = {
        super.toggle()

        wikipediaInfo match {
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
                extract = wikipediaInfo.map(_.extract).getOrElse(emptyStateText)
            } yield {
                extract
            }
        } getOrElse "-"
    }
}

class ArtistWikipediaModule[A <: CusackReceiver with Context](receiver:A) extends WikipediaModule(receiver, null) {
    def wikipediaKnowledge : Option[WikipediaKnowledgable] = currentSong.map(_.artist)
    def emptyStateText : String = "Search for artist info"
}

class AlbumWikipediaModule[A <: CusackReceiver with Context](receiver:A) extends WikipediaModule(receiver, null) {
    override def logo = Some(R.drawable.cassette_tape)

    def wikipediaKnowledge : Option[WikipediaKnowledgable] = currentSong.map(_.album)
    def emptyStateText : String = "Search for album info"
}