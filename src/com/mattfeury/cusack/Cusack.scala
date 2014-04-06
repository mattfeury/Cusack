package com.mattfeury.cusack

import com.mattfeury.cusack.modules.LyricsModule
import com.mattfeury.cusack.modules.ModuleAdapter
import com.mattfeury.cusack.services.SongDetector

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.widget.ListView
import android.widget.TextView

case class Song(artist:String, name:String, album:String)

trait SongListener {
    var currentSong:Option[Song] = None
    var songListenerHandlers:List[Song => Unit] = List()

    def setCurrentSong(artist:String, name:String, album:String) = {
        val song = Song(artist, name, album)
        currentSong = Some(song)

        songListenerHandlers.foreach(handler => handler(song))
    }

    def registerSongListener(fn:Song => Unit) = {
        songListenerHandlers ::= fn
    } 
}

trait CusackReceiver <: Activity {
    def openURIIntent(uri:String) = {
        def intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(intent)
    }
}

class Cusack extends Activity with SongListener with CusackReceiver {

    override def onCreate(savedInstanceState : Bundle) = {
        super.onCreate(savedInstanceState)

        // Setup listeners
        registerSongListener(songChanged _)

        val modules = List(
            new LyricsModule(this)
        )

        modules.foreach(module => registerSongListener(module.songChanged _))

        val mReceiver = new SongDetector(this)
        setContentView(R.layout.activity_cusack)

        def moduleList = findViewById(R.id.moduleList).asInstanceOf[ListView]
        def adapter = new ModuleAdapter(this, R.layout.module_view, modules)

        moduleList.setAdapter(adapter)
    }

    override def onCreateOptionsMenu(menu : Menu) : Boolean = {
        getMenuInflater().inflate(R.menu.activity_cusack, menu)
        true
    }

    def songChanged(song:Song) = {
        changeSongField("artistName", song, song => song.artist)
        changeSongField("songName", song, song => song.name)
        changeSongField("albumName", song, song => song.album)
    }

    private def changeSongField(viewId:String, song:Song, songFieldFactory:Song => String) = {
        def field = findViewById(getResources().getIdentifier(viewId, "id", getPackageName())).asInstanceOf[TextView]
        field.setText(songFieldFactory(song))
    }
}