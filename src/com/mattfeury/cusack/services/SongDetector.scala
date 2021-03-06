package com.mattfeury.cusack.services

import scala.collection.immutable.List
import com.mattfeury.cusack.music.NowPlaying
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.mattfeury.cusack.music.Song
import com.mattfeury.cusack.Cusack
import com.mattfeury.cusack.analytics.Mixpanel

class SongDetector extends BroadcastReceiver {

// TODO abstract these
//    val actionsToListenFor = List(
//        "com.android.music.metachanged",
//        "com.android.music.playstatechanged",
//        "com.android.music.playbackcomplete",
//        "com.android.music.queuechanged",
//        "com.android.music.metachanged",
//
//        "com.htc.music.metachanged",
//        "com.sec.android.app.music.metachanged",
//        "com.nullsoft.winamp.metachanged",
//        "com.amazon.mp3.metachanged",
//        "com.miui.player.metachanged",
//        "com.real.IMP.metachanged",
//        "com.sonyericsson.music.metachanged",
//        "com.rdio.android.metachanged",
//        "com.samsung.sec.android.MusicPlayer.metachanged",
//        "com.andrew.apollo.metachanged"
//    )

    override def onReceive(context:Context, intent:Intent) = {
        val action = intent.getAction();
        val artist = intent.getStringExtra("artist");
        val album = intent.getStringExtra("album");
        val track = intent.getStringExtra("track");
        Log.d(Cusack.TAG, action + " : " + artist + ", " + album + ", " + track);

        val isValid = List(artist, album, track).forall(field => field != null && field.trim() != "")
        if (isValid) {
            NowPlaying.setCurrentSong(artist, track, album)
        }

        Mixpanel.track("Song detected", Map(
            ("artist", artist),
            ("album", album),
            ("track", track),
            ("isValid", isValid.toString),
            ("action", action)
        ))
    }
}