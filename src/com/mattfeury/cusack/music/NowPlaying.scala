package com.mattfeury.cusack.music

import com.mattfeury.cusack.Cusack
import com.mattfeury.cusack.services.LastFmArtistInfo
import com.mattfeury.cusack.services.LastFmService
import com.mattfeury.cusack.services.MusicBrainzService
import com.mattfeury.cusack.services.MusicBrainzUriRelation
import com.mattfeury.cusack.services.TwitterInfo
import com.mattfeury.cusack.services.TwitterService
import com.mattfeury.cusack.services.WikipediaPageInfo
import com.mattfeury.cusack.services.WikipediaService
import android.os.AsyncTask
import android.util.Log
import com.mattfeury.cusack.analytics.Mixpanel
import com.mattfeury.cusack.services.MusicBrainzReleaseGroup

trait WikipediaKnowledgable {
    val name:String
    var wikipediaPageInfo:Option[WikipediaPageInfo] = None
}
case class Artist (
    name:String,

    var lastFmArtistInfo:Option[LastFmArtistInfo] = None,

    var musicBrainzId:Option[String] = None,
    var musicBrainsUriRelations:Option[List[MusicBrainzUriRelation]] = None,

    var twitterInfo:Option[TwitterInfo] = None
) extends WikipediaKnowledgable {

    private def getUrlThatMatches(closure:MusicBrainzUriRelation=>Boolean) : Option[MusicBrainzUriRelation] = {
        musicBrainsUriRelations.flatMap(_.find(closure(_)))
    }

    def getFacebookUrl() = getUrlThatMatches(_.hasHost("facebook.com"))
    def getTwitterUrl() = getUrlThatMatches(_.hasHost("twitter.com"))
}

case class Album(
    name:String,
    var musicBrainzReleaseGroup:Option[MusicBrainzReleaseGroup] = None
) extends WikipediaKnowledgable

case class Song(artist:Artist, name:String, album:Album) {
    override def toString() = s"${artist.name} - $name - ${album.name}"
    def toMap() = Map(("artist" -> artist.name), ("name" -> name), ("album" -> album.name))
}

object NowPlaying {
    var currentSong:Option[Song] = None
    var songListenerHandlers:List[Song => Unit] = List()

    def setCurrentSong(artistName:String, name:String, albumName:String) = {
        val (artist, album) = currentSong match {
            case Some(song) if song.artist.name == artistName =>
                if (song.album.name == albumName) {
                    (song.artist, song.album)
                } else {
                    (song.artist, populateAlbum(song.artist, Album(name = albumName)))
                }
            case _ =>
                (fetchArtist(artistName), Album(name = albumName))
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

    private def populateAlbum(artist:Artist, album:Album) : Album = {
        val albumTasks = List(
            new GetWikipediaAlbumExtractTask()
        )

        albumTasks.foreach(_.execute(album))

        album
    }

    class GetWikipediaArtistExtractTask extends GetWikipediaExtractTask[Artist] {
        def handler(artist:Artist, info:Option[WikipediaPageInfo]) = artist.wikipediaPageInfo = info
        def getRelations(artist:Artist) = artist.musicBrainsUriRelations.getOrElse(List())
        def getFallback(artist:Artist) = Some(artist.name)
    }

    class GetWikipediaAlbumExtractTask extends GetWikipediaExtractTask[Album] {
        def handler(album:Album, info:Option[WikipediaPageInfo]) = album.wikipediaPageInfo = info
        def getRelations(album:Album) = album.musicBrainzReleaseGroup.map(_.uriRelations).getOrElse(List())
        def getFallback(album:Album) = None
    }

    abstract class GetWikipediaExtractTask[K] extends NowPlayingTask[K] {
        def handler(k:K, info:Option[WikipediaPageInfo])
        def getRelations(k:K) : List[MusicBrainzUriRelation]
        def getFallback(k:K) : Option[String]

        def doTask(k:K) : Unit = {
            handler(k, {
                for {
                    url <- getRelations(k).find(_.hasType("wikipedia"))
                    pageTitle = WikipediaService.getPageTitleFromUrl(url.target)
                    pageInfo <- WikipediaService.getPageInfoForTitle(pageTitle)
                } yield {
                    pageInfo
                }
            } orElse {
                getFallback(k).flatMap(WikipediaService.getPageInfoForKeyword(_))
            })
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

            artist.musicBrainsUriRelations = artist.musicBrainzId.map(MusicBrainzService.getArtistUriRelations(_))

            val urlTasks = List(
                new GetTwitterTask(),
                new GetWikipediaArtistExtractTask(),
                new GetMusicBrainzAlbumInfoTask()
            )

            urlTasks.foreach(_.execute(artist))
        }
    }

    class GetTwitterTask extends NowPlayingTask[Artist] {
        def doTask(artist:Artist) : Unit = {
            artist.twitterInfo = {
                for {
                    twitterUrlRelation <- artist.getTwitterUrl()
                    handle = twitterUrlRelation.uri.getPath().substring(1)
                    latestTweet <- TwitterService.getLatestTweet(handle)
                } yield {
                    TwitterInfo(handle = handle, latestTweet = latestTweet)
                }
            }
        }
    }

    class GetMusicBrainzAlbumInfoTask extends NowPlayingTask[Artist] {
        def doTask(artist:Artist) : Unit = {
            for {
                song <- currentSong if song.album.musicBrainzReleaseGroup.isEmpty
                artistId <- song.artist.musicBrainzId
                releaseGroups = MusicBrainzService.getReleaseGroupsForArtist(artistId)
                releaseGroup <- releaseGroups.find(_.matchesTitle(song.album.name))
            } {
                song.album.musicBrainzReleaseGroup = Some(releaseGroup)

                populateAlbum(song.artist, song.album)
            }
        }
    }

    // AnyRef required due to scala/android bug: http://piotrbuda.eu/2012/12/scala-and-android-asynctask-implementation-problem.html
    abstract class NowPlayingTask[T] extends AsyncTask[AnyRef, Unit, Unit] {
        def doTask(t:T) : Unit

        override def doInBackground(refs:AnyRef*) = {
            val t = refs.toList.head.asInstanceOf[T]
            try {
                doTask(t)
            } catch {
                case e:Exception =>
                    Mixpanel.track("Error doing now playing task", Map(
                        ("class" -> this.getClass().getSimpleName()),
                        ("message" -> e.getMessage()),
                        ("song" -> currentSong.map(_.toString).getOrElse("-"))
                    ))

                    Log.e(Cusack.TAG, "Error doing now playing task:\n" + e)
                    e.printStackTrace()
                case _:Throwable =>
            }
        }

        override def onPostExecute(unit:Unit) : Unit = {
            runHandlers()
        }
    }
}