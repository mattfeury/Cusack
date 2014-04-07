package com.mattfeury.cusack.music

case class Song(artist:String, name:String, album:String) {
    override def toString() = s"$artist - $name - $album"
}

object NowPlaying {
    var currentSong:Option[Song] = None
    var songListenerHandlers:List[Song => Unit] = List()

    def setCurrentSong(artist:String, name:String, album:String) = {
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
}