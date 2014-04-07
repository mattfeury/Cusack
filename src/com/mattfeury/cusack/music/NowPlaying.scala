package com.mattfeury.cusack.music

import android.os.AsyncTask
import com.mattfeury.cusack.services.WikipediaPageInfo
import com.mattfeury.cusack.services.WikipediaService

case class Artist(name:String, var wikipediaPageInfo:Option[WikipediaPageInfo] = None, var image:Option[String] = None)

case class Song(artist:Artist, name:String, album:String) {
    override def toString() = s"$artist - $name - $album"
}

object NowPlaying {
    var currentSong:Option[Song] = None
    var songListenerHandlers:List[Song => Unit] = List()

    def setCurrentSong(artistName:String, name:String, album:String) = {
        val artist = currentSong match {
            case Some(song) if song.artist.name == artistName =>
                song.artist
            case _ =>
                fetchArtist(artistName)
        }

        val song = Song(artist, name, album)
        currentSong = Some(song)

        runHandlers()
    }

    def registerSongListener(fn:Song => Unit) = {
        songListenerHandlers ::= fn
    }

    def runHandlers() = currentSong match {
        case Some(song) => songListenerHandlers.foreach(handler => handler(song))
        case _ =>
    }

    def clearListeners() = songListenerHandlers = List()

    private def fetchArtist(artistName:String) : Artist = {
        val artist = Artist(name = artistName)

        val task = new GetWikipediaExtractTask()
        task.execute(artist)

        artist
    }

    // Why so many Units?
    class GetWikipediaExtractTask extends AsyncTask[AnyRef, Unit, Unit] {
        // AnyRef required due to scala/android bug: http://piotrbuda.eu/2012/12/scala-and-android-asynctask-implementation-problem.html
        override def doInBackground(artists:AnyRef*) = {
            val artist = artists.toList.head.asInstanceOf[Artist]
            artist.wikipediaPageInfo = WikipediaService.getPageInfoForKeyword(artist.name)
        }

        override def onPostExecute(unit:Unit) : Unit = {
            runHandlers()
        }
    }
}