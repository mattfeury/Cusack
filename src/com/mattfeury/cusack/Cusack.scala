package com.mattfeury.cusack

import com.mattfeury.cusack.modules._
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
object SongListener extends SongListener

trait CusackReceiver <: Activity {
    var adapter:Option[ModuleAdapter] = None

    def openURIIntent(uri:String) = {
        def intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(intent)
    }

    def redraw() {
        adapter.foreach(_.notifyDataSetChanged())
    }
}

class Cusack extends Activity with CusackReceiver {

    override def onCreate(savedInstanceState : Bundle) = {
        super.onCreate(savedInstanceState)

        // Setup listeners. Does using this as a singleton make us more prone to memory leaks?
        SongListener.registerSongListener(songChanged _)

        val modules = List(
            new WikipediaModule(this),
            new LyricsModule(this)
        )

        modules.foreach(module => SongListener.registerSongListener(module.songChanged _))

        setContentView(R.layout.activity_cusack)

        val moduleList = findViewById(R.id.moduleList).asInstanceOf[ListView]
        val moduleAdapter = new ModuleAdapter(this, R.layout.module_view, modules)
        adapter = Some(moduleAdapter)
        moduleList.setAdapter(moduleAdapter)

        // Redraw everything. TODO we should make sure that all the modules have updated first
        SongListener.registerSongListener(_ => moduleAdapter.notifyDataSetChanged())
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