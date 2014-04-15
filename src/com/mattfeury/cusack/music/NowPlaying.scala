package com.mattfeury.cusack.music

import android.os.AsyncTask
import com.mattfeury.cusack.services.WikipediaPageInfo
import com.mattfeury.cusack.services.WikipediaService
import com.mattfeury.cusack.services.LastFmArtistInfo
import com.mattfeury.cusack.services.LastFmService
import com.mattfeury.cusack.services.MusicBrainzArtistInfo
import com.mattfeury.cusack.services.MusicBrainzService

case class Artist(
    name:String,
    var wikipediaPageInfo:Option[WikipediaPageInfo] = None,
    var lastFmArtistInfo:Option[LastFmArtistInfo] = None,

    var musicBrainzId:Option[String] = None,
    var musicBrainzArtistInfo:Option[MusicBrainzArtistInfo] = None
)

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

        val artistTasks = List(
            new GetLastFmArtistInfoTask(),
            new MusicBrainzArtistInfoTask()
        )

        artistTasks.foreach(_.execute(artist))

        artist
    }

    class GetWikipediaExtractTask extends NowPlayingTask[Artist] {
        def doTask(artist:Artist) : Unit = {
            artist.wikipediaPageInfo = WikipediaService.getPageInfoForKeyword(artist.name)
        }
    }

    class GetLastFmArtistInfoTask extends NowPlayingTask[Artist] {
        def doTask(artist:Artist) : Unit = {
            artist.lastFmArtistInfo = LastFmService.getArtistInfo(artist.name)
        }
    }

    class MusicBrainzArtistInfoTask extends NowPlayingTask[Artist] {
        def doTask(artist:Artist) : Unit = {
            artist.musicBrainzId = MusicBrainzService.getArtistId(artist.name)

            artist.wikipediaPageInfo = {
                for {
                    id <- artist.musicBrainzId
                    urls = MusicBrainzService.getArtistUriRelations(id)
                    url <- urls.find(_.`type` == "wikipedia")
                    pageTitle = WikipediaService.getPageTitleFromUrl(url.target)
                    pageInfo <- WikipediaService.getPageInfoForTitle(pageTitle)
                } yield {
                    pageInfo
                }
            } orElse {
                WikipediaService.getPageInfoForKeyword(artist.name)
            }
        }
    }

    // AnyRef required due to scala/android bug: http://piotrbuda.eu/2012/12/scala-and-android-asynctask-implementation-problem.html
    abstract class NowPlayingTask[T] extends AsyncTask[AnyRef, Unit, Unit] {
        def doTask(t:T) : Unit

        override def doInBackground(refs:AnyRef*) = {
            val t = refs.toList.head.asInstanceOf[T]
            doTask(t)
        }

        override def onPostExecute(unit:Unit) : Unit = {
            runHandlers()
        }
    }
}