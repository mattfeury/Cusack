package com.mattfeury.cusack

import com.mattfeury.cusack.analytics.Mixpanel
import com.mattfeury.cusack.modules.AlbumWikipediaModule
import com.mattfeury.cusack.modules.ArtistWikipediaModule
import com.mattfeury.cusack.modules.ImageModule
import com.mattfeury.cusack.modules.LastFmBioModule
import com.mattfeury.cusack.modules.LyricsModule
import com.mattfeury.cusack.modules.ModuleAdapter
import com.mattfeury.cusack.modules.SongInfoModule
import com.mattfeury.cusack.modules.TwitterModule
import com.mattfeury.cusack.music.NowPlaying
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.content.Context
import android.view.LayoutInflater

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
    final def TAG = "CUSACK"
}

class Cusack extends FragmentActivity with CusackReceiver {

    override def onCreate(savedInstanceState:Bundle) = {
        super.onCreate(savedInstanceState)

        Mixpanel.setup(this)

        if (savedInstanceState == null) {
            Mixpanel.track("App Loaded")
        }

        // Setup listeners. Does using this as a singleton make us more prone to memory leaks?
        NowPlaying.clearListeners()

        val modules = List(
            new ImageModule(this),
            new SongInfoModule(this),
            new ArtistWikipediaModule(this),
            new LastFmBioModule(this),
            new AlbumWikipediaModule(this),
            new LyricsModule(this),
            new TwitterModule(this)
        )

        modules.foreach(module => NowPlaying.registerSongListener(module.songChanged _))
        NowPlaying.registerSongListener(song => {
            Mixpanel.identify(Map("hasListened" -> "true"))
        })

        setContentView(R.layout.activity_cusack)

        val moduleList = findViewById(R.id.moduleList).asInstanceOf[ListView]
        val moduleAdapter = new ModuleAdapter(this, R.layout.module_view, modules)
        adapter = Some(moduleAdapter)
        moduleList.setAdapter(moduleAdapter)

        // Redraw everything. TODO we should make sure that all the modules have updated first
        NowPlaying.registerSongListener(_ => moduleAdapter.notifyDataSetChanged())

        NowPlaying.runHandlers()

        showWelcome()
    }

    val welcomeName = "spotifyBug-10-14"
    val WELCOME_DIALOG = 0
    def showWelcome() {
        val prefs = getPreferences(Context.MODE_PRIVATE)
        val shouldShow = prefs.getBoolean(welcomeName, true)
        if (shouldShow) {
            val editor = prefs.edit()
            editor.putBoolean(welcomeName, false)
            editor.commit()
            showDialog(WELCOME_DIALOG)
        }
    }

    protected override def onCreateDialog(id:Int) : Dialog = {
        val builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
        id match {
            case WELCOME_DIALOG =>
                builder
                    .setTitle("Spotify bug fixed")
                    .setView(LayoutInflater.from(this).inflate(R.layout.welcome_layout, null))
                    .setCancelable(false)
                    .setPositiveButton("Groovy", new DialogInterface.OnClickListener() {
                        def onClick(dialog: DialogInterface, id: Int) {}
                    })
            case _ =>
        }

        val alert = builder.create()
        return alert
    }

    override def onCreateOptionsMenu(menu:Menu) : Boolean = {
        getMenuInflater().inflate(R.menu.activity_cusack, menu)
        true
    }

    protected override def onDestroy() {
        Mixpanel.flush()
        super.onDestroy()
    }

    def showAbout(menuItem:MenuItem) = {
        new AboutDialogFragment().show(getFragmentManager, "about")
        Mixpanel.track("Show About dialog")
    }
}

// TODO get this outta here
class AboutDialogFragment extends DialogFragment {
    override def onCreateDialog(savedInstanceState:Bundle) : Dialog = {
        val builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK)

        builder
            .setMessage(R.string.about_text)
            .setPositiveButton("Coolio", DialogClickHandler((d, id) => {}))
            .setNeutralButton("Cher\nthe app", DialogClickHandler((dialog, id) => {
                Mixpanel.track("Tap Share (about dialog)")

                val sendIntent = new Intent(android.content.Intent.ACTION_SEND)
                sendIntent.putExtra(Intent.EXTRA_TEXT, getResources.getString(R.string.share_text))
                sendIntent.setType("text/plain")
                startActivity(Intent.createChooser(sendIntent, "Share Cusack with others"))
            }))
            .setNegativeButton("Leave\nsome feedback", DialogClickHandler((dialog, id) => {
                Mixpanel.track("Tap Leave Feedback (about dialog)")

                val emailIntent = new Intent(android.content.Intent.ACTION_SEND)
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, Array(getResources.getString(R.string.feedback_email_address)))
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, Array(getResources.getString(R.string.feedback_email_subject)))
                emailIntent.setType("text/plain")
                startActivity(Intent.createChooser(emailIntent, "Send feedback email..."))
            }))

        builder.create()
    }
}

case class DialogClickHandler(fn:(DialogInterface, Int) => Unit) extends DialogInterface.OnClickListener() {
    def onClick(dialog:DialogInterface, id:Int) = fn(dialog, id)
}
