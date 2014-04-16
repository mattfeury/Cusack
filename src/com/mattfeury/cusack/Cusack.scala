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
import android.util.Log
import com.mattfeury.cusack.music.NowPlaying
import com.mattfeury.cusack.music.Song

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

object Cusack {
    final def TAG = "Cusack"
}

class Cusack extends Activity with CusackReceiver {

    override def onCreate(savedInstanceState:Bundle) = {
        super.onCreate(savedInstanceState)

        // Setup listeners. Does using this as a singleton make us more prone to memory leaks?
        NowPlaying.clearListeners()

        val modules = List(
            new ImageModule(this),
            new SongInfoModule(this),
            new WikipediaModule(this),
            new LyricsModule(this),
            new TwitterModule(this)
        )

        modules.foreach(module => NowPlaying.registerSongListener(module.songChanged _))

        setContentView(R.layout.activity_cusack)

        val moduleList = findViewById(R.id.moduleList).asInstanceOf[ListView]
        val moduleAdapter = new ModuleAdapter(this, R.layout.module_view, modules)
        adapter = Some(moduleAdapter)
        moduleList.setAdapter(moduleAdapter)

        // Redraw everything. TODO we should make sure that all the modules have updated first
        NowPlaying.registerSongListener(_ => moduleAdapter.notifyDataSetChanged())

        NowPlaying.runHandlers()
    }

    override def onCreateOptionsMenu(menu:Menu) : Boolean = {
        getMenuInflater().inflate(R.menu.activity_cusack, menu)
        true
    }
}